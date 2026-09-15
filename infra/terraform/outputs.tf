output "resource_group_name" {
  description = "Resource group that contains the AIOps foundation."
  value       = azurerm_resource_group.this.name
}

output "container_registry_login_server" {
  description = "ACR hostname that the deployment workflow will publish the CRM image to."
  value       = azurerm_container_registry.this.login_server
}

output "container_app_environment_id" {
  description = "Container Apps environment ID for the CRM deployment stage."
  value       = azurerm_container_app_environment.this.id
}
