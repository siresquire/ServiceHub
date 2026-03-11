# =============================================================================
# ServiceHub — ALB Module Variables
# =============================================================================

variable "name_prefix" {
  description = "Prefix for all resource names (e.g., 'servicehub-staging')"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnet IDs for the ALB"
  type        = list(string)
}

variable "alb_security_group_id" {
  description = "Security group ID for the ALB"
  type        = string
}

variable "container_port" {
  description = "Port that the target containers listen on"
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Path for target group health checks"
  type        = string
  default     = "/actuator/health"
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}
