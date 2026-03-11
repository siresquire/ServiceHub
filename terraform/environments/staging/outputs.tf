# =============================================================================
# ServiceHub — Staging Environment Outputs
# =============================================================================

# ─── Networking ──────────────────────────────────────────────────────────────
output "vpc_id" {
  description = "VPC ID"
  value       = module.networking.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs (ALB)"
  value       = module.networking.public_subnet_ids
}

output "private_subnet_ids" {
  description = "Private subnet IDs (ECS, RDS)"
  value       = module.networking.private_subnet_ids
}

# ─── ECR ─────────────────────────────────────────────────────────────────────
output "ecr_repository_url" {
  description = "ECR repository URL"
  value       = module.ecr.repository_url
}

# ─── RDS ─────────────────────────────────────────────────────────────────────
output "rds_endpoint" {
  description = "RDS endpoint address"
  value       = module.rds.endpoint
}

output "rds_port" {
  description = "RDS endpoint port"
  value       = module.rds.port
}

# ─── ALB ─────────────────────────────────────────────────────────────────────
output "alb_dns_name" {
  description = "ALB DNS name (use this to access the application)"
  value       = module.alb.alb_dns_name
}

# ─── ECS ─────────────────────────────────────────────────────────────────────
output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = module.ecs.service_name
}

output "ecs_task_definition_family" {
  description = "Task definition family (used by CI/CD to register new revisions)"
  value       = module.ecs.task_definition_family
}

# ─── GitHub Auth ─────────────────────────────────────────────────────────────
output "github_actions_role_arn" {
  description = "The IAM Role ARN to use as the AWS_ROLE_ARN secret in GitHub Actions"
  value       = module.github_oidc.role_arn
}
