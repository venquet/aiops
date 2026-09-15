package com.venquet.aiops.crm.common;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " " + id + " was not found");
    }
}
