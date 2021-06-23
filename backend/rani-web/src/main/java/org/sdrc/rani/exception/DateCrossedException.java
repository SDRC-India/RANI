package org.sdrc.rani.exception;

/**
 * @author subham
 *
 */
public class DateCrossedException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8380003525910525791L;

	public DateCrossedException() {
		super();
	}

	public DateCrossedException(String args) {
		super(args);
	}

	public DateCrossedException(Throwable args) {
		super(args);
	}

}
