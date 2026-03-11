# =============================================================================
# ServiceHub — VPC Endpoints Module
# =============================================================================
# Creates VPC endpoints so ECS Fargate tasks in private subnets can reach
# AWS services without a NAT Gateway.
#
# Endpoints:
#   - S3 (Gateway — free)
#   - ECR DKR (Interface — Docker pulls)
#   - ECR API (Interface — ECR API calls)
#   - CloudWatch Logs (Interface — container logging)
#   - SSM (Interface — secrets retrieval)
# =============================================================================

# ─── S3 Gateway Endpoint (free) ─────────────────────────────────────────────
resource "aws_vpc_endpoint" "s3" {
  vpc_id            = var.vpc_id
  service_name      = "com.amazonaws.${var.aws_region}.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [var.private_route_table_id]

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-vpce-s3"
  })
}

# ─── ECR Docker (Interface) ─────────────────────────────────────────────────
resource "aws_vpc_endpoint" "ecr_dkr" {
  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.ecr.dkr"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.security_group_id]
  private_dns_enabled = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-vpce-ecr-dkr"
  })
}

# ─── ECR API (Interface) ────────────────────────────────────────────────────
resource "aws_vpc_endpoint" "ecr_api" {
  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.ecr.api"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.security_group_id]
  private_dns_enabled = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-vpce-ecr-api"
  })
}

# ─── CloudWatch Logs (Interface) ────────────────────────────────────────────
resource "aws_vpc_endpoint" "logs" {
  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.logs"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.security_group_id]
  private_dns_enabled = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-vpce-logs"
  })
}

# ─── SSM (Interface) ────────────────────────────────────────────────────────
resource "aws_vpc_endpoint" "ssm" {
  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.ssm"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.security_group_id]
  private_dns_enabled = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-vpce-ssm"
  })
}
