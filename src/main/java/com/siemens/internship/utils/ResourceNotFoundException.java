package com.siemens.internship.utils;

/**
 * Exception thrown when a requested resource (e.g., Item) is not found.
 * Results in a 404 response via CustomExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * @param message - detail message explaining which resource was missing
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
