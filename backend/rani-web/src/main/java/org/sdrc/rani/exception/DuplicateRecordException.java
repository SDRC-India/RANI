package org.sdrc.rani.exception;

/**
 * @author subham
 *
 */
public class DuplicateRecordException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8157602321334779958L;

	public DuplicateRecordException() {
		super();
	}

	public DuplicateRecordException(String args) {
		super(args);
	}

	public DuplicateRecordException(Throwable args) {
		super(args);
	}

}
