# =============================================================================
# ServiceHub — ALB Module Outputs
# =============================================================================

output "alb_dns_name" {
  description = "DNS name of the ALB (use this to access the application)"
  value       = aws_lb.this.dns_name
}

output "alb_zone_id" {
  description = "Route 53 zone ID of the ALB (for DNS alias records)"
  value       = aws_lb.this.zone_id
}

output "alb_arn" {
  description = "ARN of the ALB"
  value       = aws_lb.this.arn
}

output "target_group_arn" {
  description = "ARN of the target group (passed to ECS service)"
  value       = aws_lb_target_group.this.arn
}

output "http_listener_arn" {
  description = "ARN of the HTTP listener"
  value       = aws_lb_listener.http.arn
}
