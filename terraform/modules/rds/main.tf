resource "aws_security_group" "rds" {
  name        = "${var.instance_identifier}-sg"
  description = "Security group for RDS instance"
  vpc_id      = var.vpc_id

  ingress {
    description = "Postgres access"
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidrs
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_subnet_group" "this" {
  name       = "${var.instance_identifier}-subnet-group"
  subnet_ids = var.db_subnet_ids
}

resource "aws_db_instance" "this" {
  identifier              = var.instance_identifier
  engine                  = "postgres"
  engine_version          = var.engine_version
  instance_class          = var.instance_class
  allocated_storage       = var.allocated_storage
  max_allocated_storage   = var.max_allocated_storage

  db_name  = var.db_name
  username = var.username
  password = var.password

  publicly_accessible    = true
  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  storage_encrypted       = true
  backup_retention_period = 7
  deletion_protection     = true
  skip_final_snapshot     = false
}
