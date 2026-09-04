package synq_backend.common.exception;

// Thrown when an operation cannot be performed on a message.
public class InvalidMessageOperationException extends RuntimeException {

    public InvalidMessageOperationException(String message) {
        super(message);
    }
}