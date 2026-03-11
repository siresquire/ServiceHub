terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0"
    }
  }
}

provider "aws" {
  region = "eu-west-1"
}

resource "aws_vpc" "this" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "servicehub-shared-dev-vpc"
  }
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "servicehub-shared-dev-igw"
  }
}

resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "eu-west-1a"
  map_public_ip_on_launch = true

  tags = {
    Name = "servicehub-shared-dev-public-a"
  }
}

resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "eu-west-1b"
  map_public_ip_on_launch = true

  tags = {
    Name = "servicehub-shared-dev-public-b"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }

  tags = {
    Name = "servicehub-shared-dev-public-rt"
  }
}

resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_b" {
  subnet_id      = aws_subnet.public_b.id
  route_table_id = aws_route_table.public.id
}

module "rds" {
  source = "../../modules/rds"

  vpc_id = aws_vpc.this.id
  db_subnet_ids = [
    aws_subnet.public_a.id,
    aws_subnet.public_b.id,
  ]

  db_name  = "servicehub"
  username = "servicehub_shared"
  password = var.rds_password

  # TEMP: open to all IPs; tighten later by changing this list
  allowed_cidrs           = ["0.0.0.0/0"]
  allowed_security_groups = []
  publicly_accessible     = true
}

output "rds_endpoint" {
  description = "Shared RDS endpoint for developers/data engineers"
  value       = module.rds.endpoint
}

output "rds_port" {
  description = "Shared RDS port"
  value       = module.rds.port
}
