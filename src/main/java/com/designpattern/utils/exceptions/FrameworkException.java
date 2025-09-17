package com.designpattern.utils.exceptions;

/**
 * Custom exception class for framework-specific errors
 */
public class FrameworkException extends RuntimeException {
    
    public FrameworkException(String message) {
        super(message);
    }
    
    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FrameworkException(Throwable cause) {
        super(cause);
    }
}