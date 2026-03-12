## What does this PR do?

Implements the foundation for a unified CI/CD Pipeline and AWS Staging Infrastructure for the ServiceHub project, alongside critical backend integration test fixes. Specifically, this PR introduces:

1. **AWS OIDC Authentication Setup:**
   - Configured GitHub Actions OIDC provider in AWS IAM
   - Created `GitHubActionsDeployRole` with proper trust policy for repository `siresquire/ServiceHub`
   - Attached required policies: ECR, ECS, and ELB permissions
   - Eliminates need for long-lived AWS access keys

2. **CI/CD Pipeline Improvements:**
   - Fixed reusable workflow output passing by using underscores instead of hyphens in output names
   - Added explicit secret declarations in `_reusable-publish-ecr.yml`
   - Implemented debug logging to track workflow data flow
   - Updated `_reusable-build-backend-image.yml` to properly expose `image_name` and `image_tag` outputs
   - Modified `deploy.yml` to consume corrected outputs from build workflow

3. **Integration Test Fixes:**
   - Resolved Postgres connection issues in CI by adding explicit wait-for-postgres logic
   - Fixed database driver and dialect configuration for Hibernate auto-detection
   - Added proper environment variable passing to test containers

4. **Spring Security Fixes:**
   - Fixed compilation error in `SecurityConfig.java` by changing `hasRole` to `hasAnyRole`
   - Added `/actuator/**` to `permitAll()` for ALB health checks without JWT authentication
   - Ensures load balancers can perform health checks on deployed services

5. **Repository Hygiene:**
   - Updated `.gitignore` to exclude `scripts/` directory containing sensitive patterns
   - Removed utility scripts from version control (available locally for AWS setup)

## Current Status

⚠️ **Work in Progress** - The pipeline is functional but still being refined:
- ✅ OIDC authentication working
- ✅ Docker image builds successfully
- ✅ ECR login successful
- 🔄 Workflow output passing being debugged (underscore naming fix applied)
- ⏳ Full end-to-end deployment pending output fix verification

## Related Issue / Task

Addresses Phase 4: Milestone 1 - CI/CD Pipeline Foundation and AWS OIDC Setup

## Type of Change

- [x] 🐛 Bug fix (non-breaking change that fixes an issue)
- [ ] ✨ New feature (non-breaking change that adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to change)
- [x] 📝 Documentation update
- [x] 🔧 DevOps / Infrastructure change
- [ ] ♻️ Refactoring (no functional changes)

## How Has This Been Tested?

- [x] Unit tests pass (`mvn test`) via CI
- [x] Integration tests pass with Postgres wait logic
- [x] Docker build works (`docker build -t servicehub-backend:test ./backend`)
- [x] AWS OIDC authentication verified via CLI scripts
- [x] ECR login successful in GitHub Actions
- [ ] Full deployment to ECS pending (blocked by workflow output debugging)

## Checklist

- [x] My code follows the project code style
- [x] I have added/updated comments explaining non-obvious logic
- [x] I have added tests that prove my fix/feature works
- [x] All existing tests pass locally
- [x] I have updated documentation if needed
- [x] My branch is up to date with `develop` / `main`

## Next Steps

1. Verify workflow outputs are passing correctly with underscore naming
2. Complete end-to-end deployment test to ECS
3. Validate ALB health check integration
4. Document final deployment procedures

## Screenshots (if applicable)

N/A - Infrastructure & Configuration changes only

## Additional Notes

**GitHub Secrets Required:**
- `AWS_ROLE_ARN`: `arn:aws:iam::904570587823:role/GitHubActionsDeployRole`
- `AWS_REGION`: Your AWS region (e.g., `eu-west-1`)
- `AWS_ACCOUNT_ID`: `904570587823`

**Breaking Changes:**
- Workflow output names changed from `image-name`/`image-tag` to `image_name`/`image_tag` for GitHub Actions compatibility
