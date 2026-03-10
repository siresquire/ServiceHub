variable "vpc_id" {
  description = "ID of the VPC where RDS will be created"
  type        = string
}

variable "db_subnet_ids" {
  description = "List of subnet IDs for the RDS subnet group"
  type        = list(string)
}

variable "db_name" {
  description = "Name of the initial database to create"
  type        = string
  default     = "servicehub"
}

variable "username" {
  description = "Master username for the RDS instance"
  type        = string
  default     = "servicehub_shared"
}

variable "password" {
  description = "Master password for the RDS instance"
  type        = string
  sensitive   = true
}

variable "allowed_cidrs" {
  description = "CIDR blocks allowed to connect to Postgres (initially 0.0.0.0/0, tighten later)"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "instance_identifier" {
  description = "Identifier for the RDS instance"
  type        = string
  default     = "servicehub-shared"
}

variable "instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "engine_version" {
  description = "Optional PostgreSQL engine version (null = let AWS pick latest compatible)"
  type        = string
  default     = null
}

variable "allocated_storage" {
  description = "Initial allocated storage in GB"
  type        = number
  default     = 20
}

variable "max_allocated_storage" {
  description = "Maximum storage in GB for autoscaling"
  type        = number
  default     = 100
}
