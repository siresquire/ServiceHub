# =============================================================================
# ServiceHub — Networking Module Outputs
# =============================================================================

# ─── VPC ─────────────────────────────────────────────────────────────────────
output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.this.id
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  value       = aws_vpc.this.cidr_block
}

# ─── Subnets ────────────────────────────────────────────────────────────────
output "public_subnet_ids" {
  description = "List of public subnet IDs (for ALB)"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "List of private subnet IDs (for ECS, RDS)"
  value       = aws_subnet.private[*].id
}

# ─── Route Tables ───────────────────────────────────────────────────────────
output "private_route_table_id" {
  description = "ID of the private route table (for VPC endpoint associations)"
  value       = aws_route_table.private.id
}

# ─── Security Groups ────────────────────────────────────────────────────────
output "alb_sg_id" {
  description = "Security group ID for the ALB"
  value       = aws_security_group.alb.id
}

output "ecs_sg_id" {
  description = "Security group ID for ECS Fargate tasks"
  value       = aws_security_group.ecs.id
}

output "rds_sg_id" {
  description = "Security group ID for the RDS instance"
  value       = aws_security_group.rds.id
}

output "vpc_endpoints_sg_id" {
  description = "Security group ID for VPC interface endpoints"
  value       = aws_security_group.vpc_endpoints.id
}

# ─── NAT Gateway ─────────────────────────────────────────────────────────────
output "nat_gateway_id" {
  description = "ID of the NAT Gateway (null if disabled)"
  value       = var.enable_nat_gateway ? aws_nat_gateway.this[0].id : null
}
