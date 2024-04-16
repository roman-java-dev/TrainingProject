package org.example.exception;

/**
 * CustomFileException is a subclass of RuntimeException used for representing an exceptional situation related to file operations.
 * It allows passing useful error messages to the user for errors that occur during file operations,
 * as well as keeping the call stack to facilitate debugging.
 */
public class CustomFileException extends RuntimeException {

    /**
     * Constructs a new CustomFileException object with the specified error message and cause.
     *
     * @param message The error message.
     * @param cause   The exception that caused this exceptional situation.
     */
    public CustomFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
