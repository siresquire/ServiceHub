# =============================================================================
# ServiceHub — Networking Module Variables
# =============================================================================

variable "name_prefix" {
  description = "Prefix for all resource names (e.g., 'servicehub-staging')"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones for subnet placement"
  type        = list(string)
  default     = ["eu-west-1a", "eu-west-1b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets (one per AZ)"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets (one per AZ)"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
}

variable "app_port" {
  description = "Application port exposed by the ECS containers"
  type        = number
  default     = 8080
}

variable "enable_nat_gateway" {
  description = "Create a NAT Gateway so private subnets can reach the internet (required for external RDS, package downloads, etc.)"
  type        = bool
  default     = false
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}
