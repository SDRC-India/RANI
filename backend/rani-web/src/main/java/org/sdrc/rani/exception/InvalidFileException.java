package org.sdrc.rani.exception;

/**
 * @author subham
 *
 */
public class InvalidFileException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6421618822528929997L;

	public InvalidFileException() {
		super();
	}

	public InvalidFileException(String args) {
		super(args);
	}

	public InvalidFileException(Throwable args) {
		super(args);
	}
}
