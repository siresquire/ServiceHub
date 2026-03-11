# =============================================================================
# ServiceHub — Staging Environment Variables
# =============================================================================

variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "eu-west-1"
}

variable "environment" {
  description = "Environment name (used for resource naming and tagging)"
  type        = string
  default     = "staging"
}

variable "rds_password" {
  description = "Master password for the RDS instance"
  type        = string
  sensitive   = true
}

variable "aws_account_id" {
  description = "AWS account ID (for constructing SSM and IAM ARNs)"
  type        = string
}
