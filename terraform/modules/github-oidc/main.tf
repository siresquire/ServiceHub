# =============================================================================
# ServiceHub — GitHub OIDC Module
# =============================================================================
# Connects GitHub Actions to AWS via OpenID Connect (OIDC).
# This allows GitHub Actions workflows to assume an AWS IAM role without
# needing long-lived access keys.
# =============================================================================

# Fetch the thumbprint for GitHub's OIDC provider automatically
data "tls_certificate" "github" {
  url = "https://token.actions.githubusercontent.com/.well-known/openid-configuration"
}

# ─── OIDC Provider ───────────────────────────────────────────────────────────
resource "aws_iam_openid_connect_provider" "github" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.github.certificates[0].sha1_fingerprint]

  tags = var.tags
}

# ─── IAM Role for GitHub Actions ─────────────────────────────────────────────
data "aws_iam_policy_document" "github_assume_role" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_repository}:*"]
    }
  }
}

resource "aws_iam_role" "github_actions" {
  name               = "${var.name_prefix}-github-actions-role"
  assume_role_policy = data.aws_iam_policy_document.github_assume_role.json
  tags               = var.tags
}

# ─── Role Permissions ────────────────────────────────────────────────────────
# For demonstration/CI purposes, attaching AdministratorAccess. 
# In a strict production setup, this should be scoped down to exactly 
# what Terraform and the application need.
resource "aws_iam_role_policy_attachment" "github_actions_admin" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}
