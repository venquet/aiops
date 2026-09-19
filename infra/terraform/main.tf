locals {
  name_prefix          = "${var.project_name}-${var.environment}"
  postgres_server_name = "psql-${local.name_prefix}-${substr(replace(var.subscription_id, "-", ""), 28, 4)}"
  tags = merge(var.tags, {
    environment = var.environment
  })
}

resource "azurerm_resource_group" "this" {
  name     = "rg-${local.name_prefix}"
  location = var.location
  tags     = local.tags
}

resource "azurerm_log_analytics_workspace" "this" {
  name                = "log-${local.name_prefix}"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = local.tags
}

resource "azurerm_application_insights" "crm" {
  name                = "appi-${local.name_prefix}-crm"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  workspace_id        = azurerm_log_analytics_workspace.this.id
  application_type    = "java"

  # This keeps the learning environment's telemetry ingestion bounded while
  # retaining enough signal for normal operation and fault-injection exercises.
  daily_data_cap_in_gb                 = 1
  daily_data_cap_notifications_enabled = true
  sampling_percentage                  = 100
  internet_ingestion_enabled           = true
  internet_query_enabled               = true
  local_authentication_enabled         = true
  tags                                 = local.tags
}

resource "azurerm_monitor_metric_alert" "crm_failed_requests" {
  name                = "alert-${local.name_prefix}-crm-failed-requests"
  resource_group_name = azurerm_resource_group.this.name
  scopes              = [azurerm_application_insights.crm.id]
  description         = "CRM has more than five failed HTTP requests in five minutes."
  severity            = 2
  frequency           = "PT1M"
  window_size         = "PT5M"

  criteria {
    metric_namespace = "microsoft.insights/components"
    metric_name      = "requests/failed"
    aggregation      = "Count"
    operator         = "GreaterThan"
    threshold        = 5
  }

  tags = local.tags
}

resource "azurerm_container_registry" "this" {
  name                = replace("acr${var.project_name}${var.environment}", "-", "")
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  sku                 = "Basic"
  admin_enabled       = false
  tags                = local.tags
}

resource "azurerm_container_app_environment" "this" {
  name                       = "cae-${local.name_prefix}"
  location                   = azurerm_resource_group.this.location
  resource_group_name        = azurerm_resource_group.this.name
  logs_destination           = "log-analytics"
  log_analytics_workspace_id = azurerm_log_analytics_workspace.this.id
  tags                       = local.tags

  workload_profile {
    name                  = "Consumption"
    workload_profile_type = "Consumption"
  }
}

resource "random_password" "postgres_admin" {
  length  = 32
  special = true
}

resource "azurerm_postgresql_flexible_server" "this" {
  name                   = local.postgres_server_name
  resource_group_name    = azurerm_resource_group.this.name
  location               = azurerm_resource_group.this.location
  version                = "16"
  administrator_login    = var.postgres_admin_username
  administrator_password = random_password.postgres_admin.result
  sku_name               = "B_Standard_B1ms"
  storage_mb             = 32768

  backup_retention_days         = 7
  geo_redundant_backup_enabled  = false
  public_network_access_enabled = true
  tags                          = local.tags

  # Azure selected an availability zone when the server was created. Keep that
  # placement instead of asking Terraform to move the server on future applies.
  lifecycle {
    ignore_changes = [zone]
  }
}

# Allows the CRM running in Azure Container Apps to reach this development server.
resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure_services" {
  name             = "allow-azure-services"
  server_id        = azurerm_postgresql_flexible_server.this.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

resource "azurerm_postgresql_flexible_server_database" "crm" {
  name      = "crm"
  server_id = azurerm_postgresql_flexible_server.this.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

resource "azurerm_user_assigned_identity" "crm_container" {
  name                = "id-${local.name_prefix}-crm"
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  tags                = local.tags
}

resource "azurerm_role_assignment" "crm_acr_pull" {
  scope                = azurerm_container_registry.this.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_user_assigned_identity.crm_container.principal_id
}

resource "azurerm_container_app" "crm" {
  name                         = "crm-${local.name_prefix}"
  resource_group_name          = azurerm_resource_group.this.name
  container_app_environment_id = azurerm_container_app_environment.this.id
  revision_mode                = "Single"
  workload_profile_name        = "Consumption"
  tags                         = local.tags

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.crm_container.id]
  }

  registry {
    server   = azurerm_container_registry.this.login_server
    identity = azurerm_user_assigned_identity.crm_container.id
  }

  secret {
    name  = "db-password"
    value = random_password.postgres_admin.result
  }

  secret {
    name  = "application-insights-connection-string"
    value = azurerm_application_insights.crm.connection_string
  }

  template {
    min_replicas = 1
    max_replicas = 2

    container {
      name   = "crm"
      image  = "${azurerm_container_registry.this.login_server}/crm:${var.container_image_tag}"
      cpu    = 0.5
      memory = "1Gi"

      env {
        name  = "DB_HOST"
        value = azurerm_postgresql_flexible_server.this.fqdn
      }
      env {
        name  = "DB_PORT"
        value = "5432"
      }
      env {
        name  = "DB_NAME"
        value = azurerm_postgresql_flexible_server_database.crm.name
      }
      env {
        name  = "DB_USERNAME"
        value = var.postgres_admin_username
      }
      env {
        name        = "DB_PASSWORD"
        secret_name = "db-password"
      }
      env {
        name        = "APPLICATIONINSIGHTS_CONNECTION_STRING"
        secret_name = "application-insights-connection-string"
      }
      env {
        name  = "OTEL_SERVICE_NAME"
        value = "aiops-crm"
      }
    }
  }

  ingress {
    external_enabled = true
    target_port      = 8080

    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  depends_on = [azurerm_role_assignment.crm_acr_pull]

  lifecycle {
    ignore_changes = [template[0].container[0].image]
  }
}
