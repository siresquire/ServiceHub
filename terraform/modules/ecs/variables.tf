# =============================================================================
# ServiceHub — ECS Fargate Module Variables
# =============================================================================

# ─── Naming ──────────────────────────────────────────────────────────────────
variable "name_prefix" {
  description = "Prefix for all resource names (e.g., 'servicehub-staging')"
  type        = string
}

variable "environment" {
  description = "Environment name (e.g., 'staging', 'production')"
  type        = string
}

variable "service_name" {
  description = "Name of the ECS service"
  type        = string
  default     = "backend"
}

variable "container_name" {
  description = "Name of the container in the task definition"
  type        = string
  default     = "servicehub-backend"
}

# ─── AWS ─────────────────────────────────────────────────────────────────────
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}

variable "aws_account_id" {
  description = "AWS account ID (for constructing SSM ARNs)"
  type        = string
}

# ─── Container ───────────────────────────────────────────────────────────────
variable "ecr_repository_url" {
  description = "ECR repository URL (e.g., 123456789.dkr.ecr.eu-west-1.amazonaws.com/servicehub-backend)"
  type        = string
}

variable "image_tag" {
  description = "Docker image tag to deploy"
  type        = string
  default     = "latest"
}

variable "container_port" {
  description = "Port exposed by the container"
  type        = number
  default     = 8080
}

variable "cpu" {
  description = "CPU units for the task (256 = 0.25 vCPU)"
  type        = number
  default     = 512
}

variable "memory" {
  description = "Memory in MiB for the task"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of task instances to run"
  type        = number
  default     = 2
}

# ─── Application ─────────────────────────────────────────────────────────────
variable "spring_profile" {
  description = "Spring Boot profile to activate"
  type        = string
  default     = "staging"
}

variable "db_name" {
  description = "Database name for SPRING_DATASOURCE_URL"
  type        = string
  default     = "servicehub"
}

variable "db_username" {
  description = "Database username for SPRING_DATASOURCE_USERNAME"
  type        = string
  default     = "servicehub_staging"
}

variable "db_password" {
  description = "Database password for SPRING_DATASOURCE_PASSWORD"
  type        = string
}

variable "jwt_secret" {
  description = "JWT Secret for application authentication"
  type        = string
}

variable "rds_endpoint" {
  description = "RDS endpoint address"
  type        = string
}

variable "rds_port" {
  description = "RDS endpoint port"
  type        = string
  default     = "5432"
}

# ─── Networking ──────────────────────────────────────────────────────────────
variable "private_subnet_ids" {
  description = "Private subnet IDs for ECS tasks"
  type        = list(string)
}

variable "ecs_security_group_id" {
  description = "Security group ID for ECS tasks"
  type        = string
}

variable "target_group_arn" {
  description = "ALB target group ARN"
  type        = string
}

# ─── Observability ───────────────────────────────────────────────────────────
variable "log_retention_days" {
  description = "CloudWatch log retention in days"
  type        = number
  default     = 30
}

variable "container_insights" {
  description = "Enable CloudWatch Container Insights on the cluster"
  type        = bool
  default     = true
}

# ─── Tags ────────────────────────────────────────────────────────────────────
variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}
