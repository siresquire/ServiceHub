# =============================================================================
# ServiceHub — VPC Endpoints Module Variables
# =============================================================================

variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

variable "aws_region" {
  description = "AWS region for constructing service names"
  type        = string
  default     = "eu-west-1"
}

variable "name_prefix" {
  description = "Prefix for resource names (e.g., 'servicehub-staging')"
  type        = string
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs for interface endpoints"
  type        = list(string)
}

variable "private_route_table_id" {
  description = "Private route table ID for the S3 gateway endpoint"
  type        = string
}

variable "security_group_id" {
  description = "Security group ID to attach to interface endpoints (must allow HTTPS from private subnets)"
  type        = string
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}
