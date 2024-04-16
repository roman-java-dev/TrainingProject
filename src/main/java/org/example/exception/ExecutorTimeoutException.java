package org.example.exception;

/**
 * ExecutorTimeoutException is a subclass of RuntimeException used for representing an exceptional situation
 * when a thread execution timeout occurs. It is intended to inform the user that the execution of a specific operation
 * did not complete within an acceptable time frame. This allows developers to make decisions based on this information.
 */
public class ExecutorTimeoutException extends RuntimeException {

    /**
     * Constructs a new ExecutorTimeoutException object with the specified error message.
     *
     * @param message The error message.
     */
    public ExecutorTimeoutException(String message) {
        super(message);
    }
}
