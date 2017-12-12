package restql.core.exception;

public class RestQLException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RestQLException(){

	}

	public RestQLException(Throwable t) {
		super(t);
	}

	public RestQLException(String cause) {
		super(cause);
	}

}
