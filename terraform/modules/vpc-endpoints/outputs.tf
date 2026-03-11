# =============================================================================
# ServiceHub — VPC Endpoints Module Outputs
# =============================================================================

output "s3_endpoint_id" {
  description = "ID of the S3 gateway endpoint"
  value       = aws_vpc_endpoint.s3.id
}

output "ecr_dkr_endpoint_id" {
  description = "ID of the ECR DKR interface endpoint"
  value       = aws_vpc_endpoint.ecr_dkr.id
}

output "ecr_api_endpoint_id" {
  description = "ID of the ECR API interface endpoint"
  value       = aws_vpc_endpoint.ecr_api.id
}

output "logs_endpoint_id" {
  description = "ID of the CloudWatch Logs interface endpoint"
  value       = aws_vpc_endpoint.logs.id
}

output "ssm_endpoint_id" {
  description = "ID of the SSM interface endpoint"
  value       = aws_vpc_endpoint.ssm.id
}
