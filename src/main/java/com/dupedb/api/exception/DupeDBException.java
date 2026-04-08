package com.dupedb.api.exception;

/**
 * Base checked exception for all DupeDB API errors.
 */
public class DupeDBException extends Exception {
    public DupeDBException(String message) {
        super(message);
    }

    public DupeDBException(String message, Throwable cause) {
        super(message, cause);
    }
}
