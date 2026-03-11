variable "repository_name" {
  description = "Name of the ECR repository"
  type        = string
  default     = "servicehub-backend"
}

variable "max_image_count" {
  description = "Maximum number of tagged images to keep in the repository"
  type        = number
  default     = 10
}

variable "tags" {
  description = "Tags to apply to the ECR repository"
  type        = map(string)
  default     = {}
}
