# Azure infrastructure

This directory defines the initial Azure foundation for the AIOps CRM:

- Resource group
- Log Analytics workspace for application and platform logs
- Azure Container Registry (ACR) for CRM images
- Azure Container Apps environment for the eventual CRM deployment

It does **not** create a database or deploy an application yet. That separation lets us review the plan, establish secure GitHub-to-Azure authentication, and choose a production-grade PostgreSQL configuration before incurring application/database costs.

## Safe local validation

```sh
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
# Replace the placeholder subscription ID in terraform.tfvars.
terraform init -backend=false
terraform fmt -check -recursive
terraform validate
```

## Before first deployment

1. Sign in locally with `az login` and choose the intended subscription.
2. Copy `terraform.tfvars.example` to the ignored `terraform.tfvars` and set the subscription ID.
3. Review `terraform plan` together before applying it.
4. Bootstrap a remote Terraform-state storage account and GitHub OIDC credentials before enabling cloud deployment automation.

The default example uses `centralindia` and `aiops-dev`; change these before deployment if they are not your intended environment.
