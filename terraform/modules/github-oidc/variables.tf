variable "name_prefix" {
  description = "Prefix for all resource names"
  type        = string
}

variable "github_repository" {
  description = "GitHub organization and repository name (e.g., 'Phase1-Group-Projects/4-ServiceHub')"
  type        = string
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}
