package com.ecommerce.project.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    String resourceName;
    String fieldName;
    String field;
    Long fieldId;

    public ResourceNotFoundException() {

    }
    public ResourceNotFoundException(String resourceName, String fieldName,Long fieldId) {
        super(String.format("Resource %s not found for %s: %d", resourceName, fieldName, fieldId));
        this.fieldName = fieldName;
        this.field = field;

    }
}
