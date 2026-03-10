# AWS Architecture Diagram Prompt

Use this prompt with an AI image generation agent (e.g., DALL·E, Midjourney, or diagram tools like Excalidraw AI) to create a professional AWS cloud architecture diagram for the backend deployment.

---

## Prompt (copy and paste)

```text
Create a professional AWS cloud architecture diagram titled:

"ServiceHub – Internal Service Request Platform (Team 10) – AWS Architecture"

The diagram must reflect a production-ready staging environment for a Java Spring Boot backend deployed on **AWS ECS Fargate** with **Amazon RDS PostgreSQL**, managed via **Terraform**, and integrated with **GitHub Actions CI/CD**. It should also show the existing **Docker Compose local environment** as the dev tier.

KEY REQUIREMENTS (THIS PROJECT, NOT A GENERIC APP):

1. TECHNOLOGY STACK (Label clearly somewhere on the diagram):
   - Backend: Java 17 / Spring Boot / Spring Security (JWT) / JPA / PostgreSQL
   - UI: Thymeleaf (server-side rendering) + Bootstrap
   - Data: Python 3.11 ETL (analytics)
   - Testing: REST Assured (API), Selenium (UI), JUnit 5
   - DevOps: Docker, Docker Compose, GitHub Actions, Terraform, AWS ECS Fargate, RDS PostgreSQL

2. HIGH-LEVEL LAYOUT (16:9 Landscape, white background):
   - LEFT: Local developer environment (Docker Compose stack)
   - CENTER: AWS Cloud (VPC with ECS Fargate, RDS, VPC Endpoints – NO NAT GATEWAY)
   - RIGHT: GitHub Actions CI/CD pipeline and Terraform resources
   - BOTTOM-RIGHT: Secrets & IAM / SSM Parameter Store

3. LEFT SIDE – LOCAL DEVELOPMENT (Gray/Light Box: "Local Dev – Docker Compose"):
   Show a Docker Compose stack with three containers:
   - servicehub-backend (Spring Boot :8080)
   - servicehub-db (PostgreSQL 16)
   - servicehub-etl (Python ETL one-shot job)
   LABEL:
   - “Run with: docker-compose up --build”
   - “Profile: dev”
   Show arrows:
   - Developer laptop → `docker-compose` → containers
   - Highlight that this is **local only**, not production.

4. CENTER – AWS CLOUD (Main focus, Light Blue VPC box):
   Title the middle section: “AWS Cloud – Staging Environment (VPC 10.0.0.0/16)”

   Inside the VPC draw:

   4.1 PUBLIC SUBNETS (2 AZs):
   - Application Load Balancer (ALB)
     - Internet-facing
     - Listeners: HTTP:80
     - Health checks: `/actuator/health`
     - Security Group: allow 80 from Internet
   - Internet Gateway (IGW) attached to VPC
   IMPORTANT: DO NOT show any NAT Gateway.

   4.2 PRIVATE SUBNETS (2 AZs):
   - ECS Fargate Cluster:
     - ECS Service: “ServiceHub Backend – Spring Boot”
     - Tasks running container:
       - Image: `servicehub-backend:staging` from Amazon ECR
       - Container Port: 8080
       - Environment:
         - `SPRING_PROFILES_ACTIVE=prod`
         - DB connection via RDS endpoint (not hard-coded in code)
         - JWT secret & DB password from SSM Parameter Store
       - Health check: `/actuator/health`
       - Logs: CloudWatch Logs
   - Optional (just one icon/box): “Scheduled Fargate Task – ETL (Python)” for analytics.
   - RDS PostgreSQL:
     - Single-AZ instance (staging)
     - PostgreSQL 16
     - Private subnets only
     - Encryption at rest
     - Security Group: allow port 5432 only from ECS tasks
   - Security groups:
     - ALB SG: 80 from Internet
     - ECS SG: 8080 from ALB SG only
     - RDS SG: 5432 from ECS SG only

   4.3 VPC ENDPOINTS (To avoid NAT Gateway – VERY IMPORTANT):
   Add a labeled sub-box inside the VPC called “VPC Endpoints – No NAT Gateway”:
   - S3 Gateway Endpoint (FREE)
   - ECR DKR Interface Endpoint
   - ECR API Interface Endpoint
   - CloudWatch Logs Interface Endpoint
   - SSM Parameter Store Interface Endpoint
   NOTES (small text in this sub-box):
   - “ECS tasks pull images from ECR via VPC Endpoints”
   - “ECS tasks send logs to CloudWatch via VPC Endpoint”
   - “Secrets (DB password, JWT secret) read from SSM via VPC Endpoint”
   Make it visually obvious that **there is NO NAT GATEWAY** and that VPC Endpoints are the path to AWS services.

5. RIGHT SIDE – CI/CD PIPELINE (Green Tint Box: “GitHub Actions CI/CD”):
   Show GitHub (source) flowing through CI/CD steps into AWS:

   - GitHub (ServiceHub repo):
     - Branches: `develop`, `feature/*`, `main`
   - GitHub Actions CI (`.github/workflows/ci.yml`):
     - Steps (bulleted or numbered):
       1. Checkout code
       2. Maven build + tests (Java backend)
       3. Flake8 lint (data-engineering)
       4. DevOps YAML lint
       5. GitLeaks scan
       6. CodeQL SAST
       7. Build backend Docker image
       8. Trivy image scan
   - GitHub Actions Deploy (`.github/workflows/deploy.yml`):
     - Steps:
       1. Checkout code
       2. Configure AWS credentials (OIDC, no long-lived keys)
       3. Build & push `servicehub-backend` image to Amazon ECR
       4. Update ECS Service (new task definition revision)
       5. Wait for service stability (ALB health checks)
       6. Smoke test `/actuator/health` via ALB URL

   Draw arrows:
   - GitHub → CI job → ECR
   - CI Deploy job → ECS Service
   - ECS Service → ALB → Users

6. BOTTOM-RIGHT – TERRAFORM & INFRASTRUCTURE-AS-CODE (Orange Tint Box: “Terraform IaC”):
   Show Terraform managing AWS resources:

   - Label: “Terraform Modules”
   - Icons/labels inside this box:
     - `networking` module:
       - VPC, Public/Private Subnets, Internet Gateway, Security Groups
       - NOTE: “NO NAT Gateway – cost-optimized via VPC Endpoints”
     - `rds` module:
       - RDS PostgreSQL instance (staging)
       - Backups, encryption
     - `ecs` module:
       - ECS Cluster, Task Definition, Service
     - `alb` module:
       - Application Load Balancer, target group, listener
     - `ecr` module:
       - ECR repository `servicehub-backend`
     - `vpc-endpoints` module:
       - S3, ECR DKR, ECR API, CloudWatch Logs, SSM endpoints
   Show a small Terraform logo with arrows pointing to:
   - VPC
   - ECS
   - RDS
   - ECR
   - VPC Endpoints

7. BOTTOM-CENTER – SECRETS & IAM (Gray Box: “Secrets & IAM”):
   - Show **SSM Parameter Store**:
     - `/servicehub/staging/db/password`
     - `/servicehub/staging/jwt/secret`
   - Label: “Secrets loaded at runtime by ECS task, not stored in code or Docker images.”
   - Show IAM Roles:
     - ECS Task Role: read from SSM, write logs to CloudWatch
     - ECS Execution Role: pull images from ECR
     - GitHub Actions OIDC Role: push images to ECR, update ECS service
   - Note: “No long-lived AWS credentials in GitHub. Uses OIDC.”

8. TOP FLOW – USER TRAFFIC:
   Show a user at the top-left:

   - [👤 Employee/User] → “HTTPS (future) / HTTP (now)” → [Internet] → [ALB] → [ECS Fargate Tasks: Spring Boot Backend] → [RDS Postgres]
   Label ServiceHub use cases:
   - Submit service request (IT_SUPPORT, FACILITIES, HR_REQUEST)
   - View dashboard & SLA metrics

9. OPTIONAL OBSERVABILITY (SMALL CALLOUT, not as big as original observability stack):
   - Add a small CloudWatch box:
     - Metrics: CPU, memory, ALB 4xx/5xx, latency
     - Logs: Spring Boot logs from ECS tasks
   - Show arrows from ECS and ALB to CloudWatch.

10. STYLE & VISUAL GUIDELINES:
   - Use official AWS orange (#FF9900) for AWS service icons.
   - Use dark blue (#232F3E) for text and borders.
   - Section backgrounds:
     - VPC / AWS Cloud: light blue
     - Local Dev: light gray
     - CI/CD: light green
     - Terraform: light orange
     - Secrets/IAM: light gray
   - Clean sans-serif font with clear labels.
   - 2px rounded borders and subtle shadows.
   - High contrast and easily readable text.

11. LEGEND (Bottom):
   Include a small legend box:
   - Blue arrows: User request flow (HTTP/HTTPS)
   - Green arrows: CI/CD deployment flow (code → image → ECS)
   - Orange arrows: Database connections (backend → RDS)
   - Dotted lines: Configuration / control paths (Terraform, IAM, SSM)
   - Note: “No NAT Gateway – traffic to AWS services via VPC Endpoints”
```
