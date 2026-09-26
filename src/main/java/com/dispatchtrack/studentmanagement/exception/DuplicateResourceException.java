package com.dispatchtrack.studentmanagement.exception;

/**
 * Thrown when an operation would violate a uniqueness constraint,
 * e.g. creating a Student with an email address that is already registered.
 * Translated by {@link GlobalExceptionHandler} into an HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
