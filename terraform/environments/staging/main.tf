# =============================================================================
# ServiceHub — Staging Environment
# =============================================================================
# Production-ready staging environment using modular Terraform.
#
# Modules:
#   - Networking: VPC, public + private subnets, security groups
#   - VPC Endpoints: S3 gateway, ECR, CloudWatch Logs, SSM interfaces
#   - ECR: Container registry with lifecycle policies
#   - RDS: PostgreSQL in private subnets, SG-locked to ECS only
#   - ALB: Internet-facing load balancer in public subnets
#   - ECS: Fargate cluster + service behind ALB
# =============================================================================

terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

locals {
  name_prefix = "servicehub-${var.environment}"

  common_tags = {
    Environment = var.environment
    Project     = "servicehub"
    ManagedBy   = "terraform"
  }
}

# ─── Networking Module ───────────────────────────────────────────────────────
module "networking" {
  source = "../../modules/networking"

  name_prefix          = local.name_prefix
  vpc_cidr             = "10.0.0.0/16"
  availability_zones   = ["${var.aws_region}a", "${var.aws_region}b"]
  public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnet_cidrs = ["10.0.10.0/24", "10.0.11.0/24"]
  app_port             = 8080
  enable_nat_gateway   = true

  tags = local.common_tags
}

# ─── VPC Endpoints Module ────────────────────────────────────────────────────
module "vpc_endpoints" {
  source = "../../modules/vpc-endpoints"

  vpc_id                 = module.networking.vpc_id
  aws_region             = var.aws_region
  name_prefix            = local.name_prefix
  private_subnet_ids     = module.networking.private_subnet_ids
  private_route_table_id = module.networking.private_route_table_id
  security_group_id      = module.networking.vpc_endpoints_sg_id

  tags = local.common_tags
}

# ─── ECR Module ──────────────────────────────────────────────────────────────
module "ecr" {
  source = "../../modules/ecr"

  repository_name = "servicehub-backend"
  max_image_count = 10

  tags = local.common_tags
}

# ─── RDS Module (Deactivated — Using Shared RDS Instance) ────────
# module "rds" {
#   source = "../../modules/rds"
#
#   vpc_id        = module.networking.vpc_id
#   db_subnet_ids = module.networking.private_subnet_ids
#
#   db_name  = "servicehub"
#   username = "servicehub_staging"
#   password = var.rds_password
#
#   allowed_cidrs           = []
#   allowed_security_groups = [module.networking.ecs_sg_id]
#   publicly_accessible     = false
# }


# ─── ALB Module ──────────────────────────────────────────────────────────────
module "alb" {
  source = "../../modules/alb"

  name_prefix           = local.name_prefix
  vpc_id                = module.networking.vpc_id
  public_subnet_ids     = module.networking.public_subnet_ids
  alb_security_group_id = module.networking.alb_sg_id
  container_port        = 8080
  health_check_path     = "/actuator/health"

  tags = local.common_tags
}

# ─── ECS Module ──────────────────────────────────────────────────────────────
module "ecs" {
  source = "../../modules/ecs"

  name_prefix        = local.name_prefix
  environment        = var.environment
  aws_region         = var.aws_region
  aws_account_id     = var.aws_account_id
  ecr_repository_url = module.ecr.repository_url
  image_tag          = "latest"

  # Networking
  private_subnet_ids    = module.networking.private_subnet_ids
  ecs_security_group_id = module.networking.ecs_sg_id
  target_group_arn      = module.alb.target_group_arn

  # Database
  rds_endpoint = "servicehub-shared.c7wm0m08amkh.eu-west-1.rds.amazonaws.com"
  rds_port     = 5432
  db_name      = "servicehub"
  db_username  = "servicehub_shared"
  db_password  = var.rds_password
  jwt_secret   = var.jwt_secret

  # Spring profile — must match an existing application-<profile>.yml
  # 'prod' loads application-prod.yml which reads SPRING_DATASOURCE_URL from env
  spring_profile = "prod"

  # Sizing
  cpu           = 512
  memory        = 1024
  desired_count = 2

  # Observability
  container_insights = true
  log_retention_days = 30

  tags = local.common_tags
}

# ─── GitHub OIDC Module ──────────────────────────────────────────────────────
module "github_oidc" {
  source = "../../modules/github-oidc"

  name_prefix       = local.name_prefix
  github_repository = "Phase1-Group-Projects/4-ServiceHub"
  tags              = local.common_tags
}
