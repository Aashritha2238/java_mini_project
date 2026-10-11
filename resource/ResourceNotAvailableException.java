package resource;

public class ResourceNotAvailableException extends Exception {
    public ResourceNotAvailableException(String message) {
        super(message);
    }
}