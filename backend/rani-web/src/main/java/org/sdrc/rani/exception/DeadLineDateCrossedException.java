package org.sdrc.rani.exception;

/**
 * @author subham
 *
 */
public class DeadLineDateCrossedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1051896835465759165L;

	public DeadLineDateCrossedException() {
		super();
	}

	public DeadLineDateCrossedException(String args) {
		super(args);
	}

	public DeadLineDateCrossedException(Throwable args) {
		super(args);
	}
}
