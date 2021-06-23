package org.sdrc.rani.exception;

import javax.servlet.http.HttpServletRequest;

import org.sdrc.usermgmt.core.util.ApiError;
import org.sdrc.usermgmt.core.util.RestExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@Component
public class ExceptionHandler extends RestExceptionHandler {

	@org.springframework.web.bind.annotation.ExceptionHandler(DuplicateRecordException.class)
	protected ResponseEntity<Object> handleDuplicateRecordException(DuplicateRecordException ex,
			HttpServletRequest request) {
		String error = ex.getMessage();
		return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, error, ex));
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(DeadLineDateCrossedException.class)
	protected ResponseEntity<Object> handleDeadLineDateCrossedException(DeadLineDateCrossedException ex,
			HttpServletRequest request) {
		String error = ex.getMessage();
		return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, error, ex));
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(DateCrossedException.class)
	protected ResponseEntity<Object> handleDateCrossedException(DateCrossedException ex, HttpServletRequest request) {
		String error = ex.getMessage();
		return buildResponseEntity(new ApiError(HttpStatus.NOT_ACCEPTABLE, error, ex));
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(InvalidFileException.class)
	protected ResponseEntity<Object> handleInvalidFileException(InvalidFileException ex, HttpServletRequest request) {
		String error = ex.getMessage();
		return buildResponseEntity(new ApiError(HttpStatus.NOT_ACCEPTABLE, error, ex));
	}
}
