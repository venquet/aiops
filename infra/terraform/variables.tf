variable "subscription_id" {
  description = "Azure subscription ID that will contain the AIOps environment."
  type        = string
  sensitive   = true
}

variable "location" {
  description = "Azure region for all foundation resources."
  type        = string
  default     = "centralindia"
}

variable "project_name" {
  description = "Short, lowercase project identifier used in resource names."
  type        = string
  default     = "aiops"

  validation {
    condition     = can(regex("^[a-z0-9]{3,12}$", var.project_name))
    error_message = "project_name must contain 3-12 lowercase letters or numbers."
  }
}

variable "environment" {
  description = "Deployment environment identifier."
  type        = string
  default     = "dev"

  validation {
    condition     = can(regex("^[a-z0-9-]{2,10}$", var.environment))
    error_message = "environment must contain 2-10 lowercase letters, numbers, or hyphens."
  }
}

variable "tags" {
  description = "Tags applied to all taggable Azure resources."
  type        = map(string)
  default = {
    managed_by = "terraform"
    workload   = "aiops-crm"
  }
}
