package org.example.exception;

/**
 * CustomExceptionHandler is an implementation of the Thread.UncaughtExceptionHandler interface
 * used for handling exceptional situations when an unfair situation occurs during the application's execution.
 * It catches exceptions that were not handled elsewhere in the code and prints an error message along with the exception message.
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**
     * Handles an uncaught exceptional situation that occurred during the application's execution.
     *
     * @param t The thread in which the exception occurred.
     * @param e The exception that was unfairly caught.
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Oops...An error has occurred. " + e.getMessage());
    }
}
