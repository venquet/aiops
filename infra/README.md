# Azure infrastructure

This directory defines the initial Azure foundation for the AIOps CRM:

- Resource group
- Log Analytics workspace for application and platform logs
- Azure Container Registry (ACR) for CRM images
- Azure Container Apps environment for the eventual CRM deployment
- Azure Database for PostgreSQL Flexible Server and the CRM database
- A public Container App with a managed identity that pulls its image from ACR

The development deployment uses a small burstable PostgreSQL 16 server, a generated password stored only in local Terraform state and the Container App secret store, and a public CRM endpoint. The database firewall rule permits Azure services so the Container App can connect; it is appropriate for this learning environment, not a production network boundary.

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
4. Bootstrap a remote Terraform-state storage account before managing infrastructure from CI.

The default example uses `centralindia` and `aiops-dev`; change these before deployment if they are not your intended environment.

## Application delivery

The `Deploy CRM to Azure` GitHub Actions workflow packages the CRM, builds an image in ACR, and updates the Container App when `app/crm` changes on `main`. It uses GitHub OIDC with a branch-scoped Azure federated credential, so GitHub stores no Azure client secret. The credential is granted `Contributor` on this development resource group and `AcrPush` on its registry.

Terraform state remains local for this learning environment. Do not add Terraform applies to GitHub Actions until state is moved to a protected remote backend.
