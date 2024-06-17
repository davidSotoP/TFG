package exportador.exceptions;

public class BookIdMismatchException extends RuntimeException {

	public BookIdMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public BookIdMismatchException(String message) {
    	super(message);
    }
}
