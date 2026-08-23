package synq_backend.common.exception;

public class ResourceNotFoundException extends  RuntimeException{

    // Thrown when a requested resource does not exist.
    public ResourceNotFoundException(String message){
        super(message);
    }
}
