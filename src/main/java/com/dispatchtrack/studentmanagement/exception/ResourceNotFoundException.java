package com.dispatchtrack.studentmanagement.exception;

/**
 * Thrown when a requested resource (e.g. a Student by id) does not exist.
 *
 * Deliberately a plain, unchecked RuntimeException so service-layer methods
 * don't need "throws" clauses everywhere. {@link GlobalExceptionHandler}
 * catches it and translates it into an HTTP 404 response.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience factory for the most common case: "Student not found with id : '5'".
     */
    public static ResourceNotFoundException forId(String resourceName, Object id) {
        return new ResourceNotFoundException(
                String.format("%s not found with id : '%s'", resourceName, id));
    }
}
