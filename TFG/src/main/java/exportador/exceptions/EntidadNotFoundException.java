package exportador.exceptions;

public class EntidadNotFoundException extends RuntimeException {

    public EntidadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EntidadNotFoundException(String message) {
    	super(message);
    }
}
