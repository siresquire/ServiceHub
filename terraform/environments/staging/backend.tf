# =============================================================================
# ServiceHub — Terraform S3 Backend Configuration (Staging)
# =============================================================================
# Uses native S3 locking (Terraform >= 1.10, use_lockfile = true).
# No DynamoDB table needed.
#
# The S3 bucket must be created FIRST using:
#   terraform/bootstrap/bootstrap-tf-backend.sh
#
# To migrate from local state to S3:
#   terraform init -migrate-state
# =============================================================================
terraform {
  backend "s3" {
    bucket       = "servicehub-terraform-state-904570587823"
    key          = "staging/terraform.tfstate"
    region       = "eu-west-1"
    encrypt      = true
    use_lockfile = true
  }
}
