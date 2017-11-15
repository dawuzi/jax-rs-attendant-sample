package com.dawuzi.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * 
 * An example of an implementation of a {@link ContainerResponseFilter} 
 * This was used to add the header "Access-Control-Allow-Origin" to ease 
 * the demo of the sse rest call from a static html file not served from 
 * the web server
 * 
 * @author DAWUZI
 *
 */

@Provider
public class AttendantResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		
		if (requestContext.getUriInfo().getPath().contains("sse")) {
			responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
		}		
	}
}
