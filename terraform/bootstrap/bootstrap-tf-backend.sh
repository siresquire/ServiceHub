#!/usr/bin/env bash
# =============================================================================
# ServiceHub — Terraform S3 Backend Bootstrap
# =============================================================================
# Creates the S3 bucket used to store Terraform state remotely.
# Uses native S3 locking (Terraform >= 1.10, GA in 1.11) — no DynamoDB needed.
#
# Usage:
#   chmod +x bootstrap-tf-backend.sh
#   ./bootstrap-tf-backend.sh
#
# Prerequisites:
#   - AWS CLI configured with admin permissions
#   - bash 4+
#
# Idempotent: safe to run multiple times.
# =============================================================================

set -euo pipefail

# ─── Configuration ───────────────────────────────────────────────────────────
AWS_REGION="${AWS_REGION:-eu-west-1}"
BUCKET_PREFIX="servicehub-terraform-state"

# ─── Colors ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; }

# ─── Pre-flight checks ──────────────────────────────────────────────────────
info "Running pre-flight checks..."

if ! command -v aws &>/dev/null; then
    error "AWS CLI is not installed. Install it first: https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html"
    exit 1
fi

CALLER_IDENTITY=$(aws sts get-caller-identity --output json 2>/dev/null) || {
    error "AWS credentials not configured. Run 'aws configure' or set AWS_PROFILE."
    exit 1
}

ACCOUNT_ID=$(echo "$CALLER_IDENTITY" | grep -o '"Account": "[^"]*"' | cut -d'"' -f4)
CALLER_ARN=$(echo "$CALLER_IDENTITY" | grep -o '"Arn": "[^"]*"' | cut -d'"' -f4)

info "AWS Account:  $ACCOUNT_ID"
info "Caller ARN:   $CALLER_ARN"
info "Region:       $AWS_REGION"

BUCKET_NAME="${BUCKET_PREFIX}-${ACCOUNT_ID}"
info "Bucket name:  $BUCKET_NAME"

echo ""

# ─── Create S3 Bucket ───────────────────────────────────────────────────────
if aws s3api head-bucket --bucket "$BUCKET_NAME" 2>/dev/null; then
    success "S3 bucket '$BUCKET_NAME' already exists. Skipping creation."
else
    info "Creating S3 bucket '$BUCKET_NAME'..."

    # eu-west-1 is not us-east-1, so we need LocationConstraint
    aws s3api create-bucket \
        --bucket "$BUCKET_NAME" \
        --region "$AWS_REGION" \
        --create-bucket-configuration LocationConstraint="$AWS_REGION"

    success "S3 bucket created."
fi

# ─── Enable Versioning ──────────────────────────────────────────────────────
info "Enabling versioning on '$BUCKET_NAME'..."
aws s3api put-bucket-versioning \
    --bucket "$BUCKET_NAME" \
    --versioning-configuration Status=Enabled

success "Versioning enabled."

# ─── Enable Server-Side Encryption (AES-256) ────────────────────────────────
info "Enabling default encryption (AES-256)..."
aws s3api put-bucket-encryption \
    --bucket "$BUCKET_NAME" \
    --server-side-encryption-configuration '{
        "Rules": [
            {
                "ApplyServerSideEncryptionByDefault": {
                    "SSEAlgorithm": "AES256"
                },
                "BucketKeyEnabled": true
            }
        ]
    }'

success "Encryption enabled."

# ─── Block All Public Access ────────────────────────────────────────────────
info "Blocking all public access..."
aws s3api put-public-access-block \
    --bucket "$BUCKET_NAME" \
    --public-access-block-configuration \
        BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

success "Public access blocked."

# ─── Add Bucket Policy to Enforce Encryption ────────────────────────────────
info "Adding bucket policy to enforce encryption in transit..."
aws s3api put-bucket-policy \
    --bucket "$BUCKET_NAME" \
    --policy "{
        \"Version\": \"2012-10-17\",
        \"Statement\": [
            {
                \"Sid\": \"EnforceTLS\",
                \"Effect\": \"Deny\",
                \"Principal\": \"*\",
                \"Action\": \"s3:*\",
                \"Resource\": [
                    \"arn:aws:s3:::${BUCKET_NAME}\",
                    \"arn:aws:s3:::${BUCKET_NAME}/*\"
                ],
                \"Condition\": {
                    \"Bool\": {
                        \"aws:SecureTransport\": \"false\"
                    }
                }
            }
        ]
    }"

success "Bucket policy applied."

# ─── Summary ────────────────────────────────────────────────────────────────
echo ""
echo "========================================="
echo -e "${GREEN} Terraform S3 Backend Ready${NC}"
echo "========================================="
echo ""
echo "Add this to your Terraform backend configuration:"
echo ""
echo "  terraform {"
echo "    backend \"s3\" {"
echo "      bucket       = \"$BUCKET_NAME\""
echo "      key          = \"staging/terraform.tfstate\""
echo "      region       = \"$AWS_REGION\""
echo "      encrypt      = true"
echo "      use_lockfile = true"
echo "    }"
echo "  }"
echo ""
echo "Then run:  terraform init -migrate-state"
echo ""
