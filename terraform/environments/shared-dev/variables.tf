variable "rds_password" {
  description = "Master password for the shared RDS instance"
  type        = string
  sensitive   = true
}
