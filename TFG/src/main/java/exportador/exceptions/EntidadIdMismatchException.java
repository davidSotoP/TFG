package exportador.exceptions;

public class EntidadIdMismatchException extends RuntimeException {

	public EntidadIdMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntidadIdMismatchException(String message) {
    	super(message);
    }
}
