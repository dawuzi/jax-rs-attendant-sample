package com.dawuzi.validation;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.dawuzi.model.Attendee;
import com.dawuzi.model.ResponseMessage;

/**
 * 
 * A {@link ExceptionMapper} implementation targeted as demonstrating how the response sent to clients
 * can be customised for specific exceptions. This was targetted at the {@link ConstraintViolationException} 
 * which is thrown when the request from client is invalid based on the constraint defined in the {@link Attendee}
 * request 
 * 
 * @author DAWUZI
 *
 */

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	private Logger logger = Logger.getLogger(getClass().getName());
	
	@Override
	public Response toResponse(ConstraintViolationException exception) {
		
		logger.log(Level.SEVERE, "Validation Exception : "+exception.getMessage());
		
		String error = exception.getConstraintViolations().stream().map(cv -> cv.getMessage()).collect(Collectors.joining(", "));
		
		return Response
				.status(Status.BAD_REQUEST)
				.entity(new ResponseMessage(error))
				.build();
	}
}
