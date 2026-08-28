package synq_backend.common.exception;

// Thrown when authentication credentials are invalid.
public class AuthenticationException extends RuntimeException{

    public AuthenticationException(String message){
        super(message);
    }
}
