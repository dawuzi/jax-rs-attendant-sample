package com.dawuzi.rough;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Priority;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.dawuzi.model.Attendee;
import com.dawuzi.model.AttendeeGenericType;

/**
 * @author DAWUZI
 *
 */

@Priority(value=1)
public class CompletionTest implements ContainerRequestFilter, ContainerResponseFilter {

	public static void main(String[] args) {

	}

	public static void test() {
		CompletionStage<String> completionStage = null;

		Client client = ClientBuilder.newClient();

		WebTarget target = client.target("");

		Response response = target.queryParam("start", "10")
				.request()
				.cookie("test-cookie", "test")
				.header("test-header", "test")
				.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_ATOM_XML_TYPE)
				.get();

	}

	public static void test2() {
		CompletionStage<String> completionStage = null;

		Client client = ClientBuilder.newClient();

		WebTarget target = client.target("");

		Attendee attendantResponse = target.queryParam("start", "10")
				.request()
				.post(Entity.json(new Attendee()), Attendee.class);

	}

	public static void test3() throws InterruptedException, ExecutionException {
		CompletionStage<String> completionStage = null;

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("");
		AsyncInvoker asyncInvoker = target.request().async();
		Future<Response> future = asyncInvoker.get();
		while(future.isDone()){ // or do some other task 
			Response response = future.get();
		}
		
		Client newClient = ClientBuilder.newClient();
		
		Future<String> futureResponse = newClient.target("someUrl")
				.request()
				.async()
				.get(String.class);
		
		futureResponse.isDone();
		
		futureResponse.isCancelled();
		
		futureResponse.get();
		
	}
	
	public static void test4() {
		
		GenericEntity<String> ge = null;
		GenericType<String> gt = null;
		
		
		Client client = ClientBuilder.newClient();
		
		List<Attendee> list = client.target("")
		.request()
		.get(new AttendeeGenericType());
		
				
		
		
	}
	

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		// TODO Auto-generated method stub

	}
	
	public void rough(){
		SecurityContext context;
	}



}
