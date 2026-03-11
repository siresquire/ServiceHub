# Developer Guide — DevOps Engineer

> **Assignee**: Prince Tetteh Ayiku (prince.ayiku@amalitech.com)
> **Branch**: `feature/devops-infra`

## Your Responsibilities

| Deliverable | Status |
|-------------|--------|
| Docker Setup (Dockerfiles for backend + database) | ✅ Done |
| Docker Compose (multi-container with proper networking) | ✅ Done |
| CI/CD Pipeline (GitHub Actions: build, test, deploy) | ✅ Done |
| Environment Config (dev, test, prod profiles) | ✅ Done |
| Deployment Docs (setup + deployment documentation) | ✅ Done |
| Terraform Infrastructure (VPC, ECR, ECS, ALB, RDS) | ✅ Done |
| AWS Deployment Pipeline (ECR → ECS Fargate) | ✅ Done |

## Files You Own

```
.github/
├── workflows/
│   ├── ci.yml                          ← CI pipeline
│   ├── ci-feature.yml                  ← Feature branch fast CI
│   ├── deploy.yml                      ← CD pipeline (ECR → ECS)
│   ├── _reusable-build-backend-image.yml
│   ├── _reusable-publish-ecr.yml       ← Push images to ECR
│   ├── _reusable-security-scans.yml
│   ├── _reusable-integration-smoke.yml
│   ├── _reusable-lint-test-backend.yml
│   ├── _reusable-lint-data-engineering.yml
│   └── _reusable-lint-devops.yml
├── CODEOWNERS                          ← Auto-reviewer assignment
└── pull_request_template.md            ← PR checklist

terraform/
├── bootstrap/
│   └── bootstrap-tf-backend.sh         ← S3 backend setup
├── environments/
│   ├── shared-dev/                     ← Dev environment (legacy)
│   └── staging/                        ← Staging (ECR + ECS + ALB + RDS)
│       ├── backend.tf                  ← S3 remote state config
│       ├── main.tf                     ← Module wiring
│       ├── variables.tf
│       ├── outputs.tf
│       └── terraform.tfvars.example
└── modules/
    ├── ecr/                            ← Container registry
    ├── networking/                     ← VPC, subnets, SGs
    ├── vpc-endpoints/                  ← Private subnet connectivity
    ├── rds/                            ← PostgreSQL database
    ├── alb/                            ← Load balancer
    └── ecs/                            ← Fargate cluster + service

.env.example                            ← Environment variable template
.gitignore                              ← Git exclusion rules
.pre-commit-config.yaml                 ← Local code quality hooks
Makefile                                ← Developer convenience commands
docker-compose.yml                      ← Main service orchestration
docker-compose.observability.yml        ← Monitoring stack (Prometheus + Grafana)

backend/
├── Dockerfile                          ← Multi-stage Java 17 build
└── .dockerignore                       ← Docker context exclusions

backend/src/main/resources/
├── application.yml                     ← Base config (env-var driven)
├── application-dev.yml                 ← Local dev overrides
├── application-test.yml                ← CI/CD test overrides
├── application-prod.yml                ← Production config
└── logback-spring.xml                  ← Structured JSON logging
```

---

## Local Development Runbook

### Starting the Full Stack
```bash
cp .env.example .env
docker-compose up --build -d
```

### Checking Service Health
```bash
docker-compose ps
curl http://localhost:8080/actuator/health
docker exec -it servicehub-db pg_isready -U servicehub
```

### Viewing Logs
```bash
docker-compose logs -f backend    # Backend only
docker-compose logs -f            # All services
```

### Full Reset (clean slate)
```bash
docker-compose down -v --remove-orphans
docker-compose up --build -d
```

---

## AWS Deployment Runbook

### Architecture Overview

```
GitHub Actions (deploy.yml)
    │
    ├── 1. Build image → artifact
    ├── 2. Push to ECR (OIDC auth)
    ├── 3. Deploy to ECS Fargate
    └── 4. Smoke test ALB /actuator/health

AWS Infrastructure (Terraform):
    VPC (10.0.0.0/16)
    ├── Public Subnets → ALB (HTTP:80)
    ├── Private Subnets → ECS Fargate (8080)
    │                  → RDS PostgreSQL (5432)
    └── VPC Endpoints → ECR, CloudWatch, SSM (no NAT)
```

### First-Time Bootstrap

#### 1. Create the S3 Backend
```bash
cd terraform/bootstrap
chmod +x bootstrap-tf-backend.sh
./bootstrap-tf-backend.sh
# Note the bucket name from the output
```

#### 2. Update Backend Config
Edit `terraform/environments/staging/backend.tf` — replace `REPLACE_WITH_ACCOUNT_ID` with your account ID.

#### 3. Set Up OIDC for GitHub Actions
- Create IAM OIDC provider for `token.actions.githubusercontent.com`
- Create IAM role `github-actions-servicehub` with:
  - ECR push/pull, ECS deploy, SSM read, IAM PassRole, ELB describe
- Scope trust policy to your GitHub org/repo

#### 4. Create SSM Parameters
```bash
aws ssm put-parameter \
  --name "/servicehub/staging/db/password" \
  --type SecureString \
  --value "YOUR_DB_PASSWORD"

aws ssm put-parameter \
  --name "/servicehub/staging/jwt/secret" \
  --type SecureString \
  --value "YOUR_JWT_SECRET"
```

#### 5. Set GitHub Secrets
| Secret | Value |
|---|---|
| `AWS_ROLE_ARN` | ARN of the OIDC IAM role |
| `AWS_REGION` | `eu-west-1` |
| `AWS_ACCOUNT_ID` | 12-digit account ID |

#### 6. Apply Terraform
```bash
cd terraform/environments/staging
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with real values
terraform init
terraform plan
terraform apply
```

### Terraform Usage

| Command | When to Use |
|---|---|
| `terraform init` | First time, or after backend/module changes |
| `terraform plan` | Always before apply — review what will change |
| `terraform apply` | Apply changes (requires `plan` review) |
| `terraform output` | Show outputs (ALB DNS, ECR URL, etc.) |
| `terraform state list` | List all managed resources |

### ECR Image Lifecycle

- Images tagged with commit SHA + `latest` on every deploy
- **Lifecycle policy**: Untagged images expire after 7 days; keep last 10 tagged
- To list images: `aws ecr describe-images --repository-name servicehub-backend`
- To manually delete: `aws ecr batch-delete-image --repository-name servicehub-backend --image-ids imageTag=<tag>`

### ECS Deployment Flow

1. Push to `main` triggers `deploy.yml`
2. Image built → pushed to ECR with SHA tag
3. New task definition rendered with updated image
4. ECS service updated (rolling deploy: min 50%, max 200%)
5. Waits for service stability (all tasks healthy)
6. Smoke test curls ALB `/actuator/health`
7. If deploy fails → **circuit breaker auto-rolls back**

### Secrets Management (SSM Parameter Store)

| Parameter | Purpose |
|---|---|
| `/servicehub/staging/db/password` | RDS master password |
| `/servicehub/staging/jwt/secret` | JWT signing key |

- Injected into ECS containers via task definition `secrets` block
- Rotated by updating SSM + redeploying ECS service
- Never stored in code, `.env`, or `terraform.tfvars`

### Rollback Procedures

#### Automatic Rollback (Circuit Breaker)
ECS circuit breaker is enabled — if new tasks fail health checks, the service automatically rolls back to the previous task definition.

#### Manual Rollback to Previous Image
```bash
# Find the previous image tag
aws ecr describe-images --repository-name servicehub-backend \
  --query 'sort_by(imageDetails,&imagePushedAt)[-5:].imageTags' --output table

# Force a new deployment with the old image
aws ecs update-service --cluster servicehub-staging-cluster \
  --service servicehub-staging-backend \
  --force-new-deployment
```

#### Terraform Rollback
```bash
# Revert to previous commit
git revert HEAD
git push

# Or roll back specific resources
terraform plan   # Review what will change
terraform apply
```

---

## GitHub Repository Setup Checklist

After creating the team repository:
- [ ] Push this codebase to the repo
- [ ] Enable branch protection on `main` (require PR, 1 approval, CI pass)
- [ ] Update `CODEOWNERS` with actual GitHub usernames
- [ ] Set up GitHub Projects Kanban board
- [ ] Add repository secrets (AWS_ROLE_ARN, AWS_REGION, AWS_ACCOUNT_ID)
- [ ] Create GitHub environment `staging` (optional: required reviewers)
- [ ] Run Terraform bootstrap and first apply
- [ ] Verify deploy pipeline on a test merge to main
