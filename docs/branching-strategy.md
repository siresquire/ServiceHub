# ServiceHub — Branching Strategy

## Overview

We use a **Git Flow-lite** strategy with two long-lived branches and short-lived feature branches.

```
main            ← Production-ready code. Protected. Deploy from here.
  │
  └── develop   ← Integration branch. All feature branches merge here.
       │
       ├── feature/auth-dashboard        (Dev C: Alphonse)
       ├── feature/request-management    (Dev A: Ange)
       ├── feature/workflow-sla          (Dev B: Fiifi)
       ├── feature/qa-tests             (QA: Zakaria)
       ├── feature/data-pipeline        (Data: Richard)
       ├── feature/devops-infra         (DevOps: Prince)
       ├── bugfix/fix-login-redirect    (example bugfix)
       └── hotfix/critical-sla-bug      (example hotfix)
```

## Branch Naming Convention

| Prefix | Use Case | Example |
|--------|----------|---------|
| `feature/` | New functionality | `feature/request-management` |
| `bugfix/` | Non-critical bug fixes | `bugfix/fix-sla-calculation` |
| `hotfix/` | Critical production fixes | `hotfix/auth-token-expired` |
| `release/` | Release preparation | `release/v1.0.0` |
| `chore/` | Maintenance tasks | `chore/update-dependencies` |
| `docs/` | Documentation only | `docs/api-contracts` |

> **Enforced by**: Pre-commit hooks and GitHub rulesets. Non-conforming branch names will be rejected.

## Workflow

### Creating a Feature Branch

```bash
# 1. Start from develop (always pull latest first)
git checkout develop
git pull origin develop

# 2. Create your feature branch
git checkout -b feature/your-feature-name

# 3. Work on your feature (commit often with conventional messages)
git add .
git commit -m "feat: add request creation endpoint"

# 4. Push your branch
git push origin feature/your-feature-name

# 5. Open a Pull Request targeting develop
```

### Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/). Every commit message must follow this format:

```
<type>: <short description>

[optional body]
```

| Type | When to Use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `test` | Adding or updating tests |
| `refactor` | Code change that doesn't fix a bug or add a feature |
| `ci` | CI/CD pipeline changes |
| `chore` | Maintenance (dependency updates, etc.) |
| `style` | Code formatting (no logic change) |
| `perf` | Performance improvement |

**Examples:**
```
feat: add SLA breach detection endpoint
fix: correct status transition from OPEN to ASSIGNED
docs: update API contracts for dashboard endpoints
test: add REST Assured tests for request creation
ci: add Docker build stage to CI pipeline
```

## Branch Protection Rules (main)

- ✅ Require pull request before merging
- ✅ Require at least 1 approving review
- ✅ Require status checks to pass (CI pipeline)
- ✅ Require CODEOWNERS review
- ❌ No direct pushes allowed

## Merge Strategy

- **Feature → Develop**: Squash merge (clean history on develop)
- **Develop → Main**: Merge commit (preserves full history for releases)

## Quick Reference

```bash
# See all branches
git branch -a

# Update your branch with latest develop
git checkout feature/your-branch
git pull origin develop --rebase

# Delete a branch after merge
git branch -d feature/your-branch
git push origin --delete feature/your-branch
```
