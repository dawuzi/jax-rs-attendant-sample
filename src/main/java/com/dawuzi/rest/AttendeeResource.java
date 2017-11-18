package com.dawuzi.rest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.CompletionStageRxInvoker;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import com.dawuzi.db.MockDatabase;
import com.dawuzi.model.Attendee;
import com.dawuzi.model.AttendeeGenericType;
import com.dawuzi.model.ResponseMessage;
import com.dawuzi.service.AttendeeService;

/**
 * @author DAWUZI
 *
 */

@Path("/attendee")
@Produces(value = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class AttendeeResource {

	private Logger logger = Logger.getLogger(getClass().getName());
	
	@Inject
	private AttendeeService attendeeService;
	
	@Inject
	private MockDatabase mockDatabase;
	
	@Resource
	ManagedExecutorService managedExecutorService;
	
	/**
	 * So actual classes (or a collection of them) representing the resource can be returned directly
	 * like this example below. However it does not give one enough flexibility like changing the 
	 * HTTP response codes, adding cookies, cache control etc which is possible when returning a 
	 * {@link Response} object obtained from a {@link ResponseBuilder} 
	 * 
	 * 
	 * @return a list of all the current attendants
	 */
	@GET
	public List<Attendee> getAllAttendees(){
		return attendeeService.getAllAttendees();
	}
	
	/**
	 * Sample to illustrate use of QueryParam
	 * 
	 * @param size
	 * @param order
	 * @return
	 */
	@GET
	@Path("/some-attendees")
	public List<Attendee> getSomeAttendees(
			@DefaultValue(value="0") @PositiveOrZero(message = "start must not be negative") @QueryParam("start") int start
			, @DefaultValue(value="10") @Positive(message = "size must be greater than zero") @QueryParam("size") int size
			, @DefaultValue(value="id") @QueryParam("order") String order
			){
		return attendeeService.getSomeAttendees(start, size, order);
	}

	/**
	 * This returns a {@link Response}. As seen in the {@link AttendeeService#getAttendee(Long)} method
	 * custom response HTTP code like 404 can be returned easily without having to throw a
	 * {@link WebApplicationException} 
	 * 
	 * @param id
	 * @return The attendant with that id
	 */
	@GET
	@Path("/{id}")
	public Response getAttendee(@PathParam("id") Long id){
		return attendeeService.getAttendee(id);
	}
	
	@POST
	public Response createAttendee(Attendee attendant, @Context UriInfo uriInfo){
		return attendeeService.createAttendee(attendant, uriInfo);
	}
	
	@PUT
	public Response updateAttendee(Attendee attendant){
		return attendeeService.updateAttendee(attendant);
	}
	
	/**
	 * This attempts to create an attendants using the same method in 
	 * {@link AttendeeResource#createAttendee(Attendee, UriInfo)}
	 * with the only difference being that Bean validation is applied to the input
	 * using the {@link Valid} annotation.
	 * Doing this will ensure that the input is valid with respect to all constraints
	 * defined in the {@link Attendee} class
	 * 
	 * @param attendant
	 * @param uriInfo
	 * @return
	 */
	@POST
	@Path("/valid")
	public Response createAttendeeValid(@Valid Attendee attendant, @Context UriInfo uriInfo){
		return attendeeService.createAttendee(attendant, uriInfo);
	}
	
	/**
	 * 
	 * http://localhost:8080/demo/sse_test.html or whatever IP and Port the application is deployed
	 * 
	 * This is a sample of how to send event streams to clients. This would send the current count of attendees
	 * to a client. This was introduced in JAX-RS 2.1
	 * 
	 * @param sseEventSink
	 * @param sse
	 */
	@GET
	@Path("/sse/attendee-count")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public void eventStream(@Context SseEventSink sseEventSink, @Context Sse sse){
		
		int lastCount = attendeeService.getAttendeeCount();
		
		if(!sseEventSink.isClosed()){
//			send at least once
			sseEventSink.send(sse.newEvent(String.valueOf(lastCount)));
		}
		
//		SseEventSink implements AutoCloseable. So it can be used in a try with resources to 
//		automatically close it once we are done
		try (SseEventSink sinker = sseEventSink){
			
			while(!sinker.isClosed()){
				int currentCount = attendeeService.getAttendeeCount();
//				so the attendee count has changed, so we send the client an updated count
				if(lastCount != currentCount){
					lastCount = currentCount;
					sinker.send(sse.newEvent(String.valueOf(lastCount)));
					
//					you can build more complex event stream to be sent to the client 
//					using the Builder as shown below. 
					
//					sseEventSink.send(sse.newEventBuilder()
//							.reconnectDelay(10000)
//							.data(String.valueOf(lastCount))
//							.build());
				}
				sleep(10000);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "An error occured send events to the client", e);
		}
	}

	/**
	 * 
	 * Handling responses asynchronously in JAX-RS 2.0
	 * 
	 * @param asyncResponse
	 */
	@GET
	@Path("/async")
	@Asynchronous
	public void getAllAttendantsAsync(@Suspended final AsyncResponse asyncResponse) {
		
		asyncResponse.setTimeout(1, TimeUnit.MINUTES);
		asyncResponse.setTimeoutHandler(new TimeoutHandler() {
			@Override
			public void handleTimeout(AsyncResponse asyncResponse) {
				asyncResponse.resume(new WebApplicationException(Status.SERVICE_UNAVAILABLE));
			}
		});
		
		asyncResponse.register(new CompletionCallback() {
			@Override
			public void onComplete(Throwable throwable) {
				logger.info("onComplete called");
			}
		});
		
		asyncResponse.register(new ConnectionCallback() {
			@Override
			public void onDisconnect(AsyncResponse disconnected) {
				logger.info("onDisconnect called");
			}
		});
		
		managedExecutorService.execute(() -> {
			sleep(10000); // simulating a long running operation
			asyncResponse.resume(mockDatabase.findAll());
		});
		
	}
	/**
	 * From JAX-RS 2.1 The {@link CompletionStage} can now be returned directly
	 * @return
	 */
	@GET
	@Path("/async21")
	public CompletionStage<List<Attendee>> getAllAttendantsAsync21() {
		
		CompletableFuture<List<Attendee>> allAttendants = CompletableFuture.supplyAsync(
				() -> {
					List<Attendee> attendants = getAttendeeViaLongRunningOperation();
					return attendants;
				}
				, managedExecutorService
				);
		
		return allAttendants;
	}	

	private List<Attendee> getAttendeeViaLongRunningOperation() {
		
		sleep(10000); // simulating a long running operation
		
		try {
			
			logger.info("About to get the list of all attendants");
			
			Client client = ClientBuilder.newClient();

			List<Attendee> attendants = client
					.target("http://localhost:8080/demo/lagjug/attendee")
					.request()
					.get(new AttendeeGenericType());
			
			logger.info("After getting the list of all attendants");

			return attendants;
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error calling client", e);
		}
		
		return Collections.emptyList();
	}
	
	@GET
	@Path("/async21Ext")
	public CompletionStage<Response> getAllAttendantsAsync21Ext() {

		CompletionStageRxInvoker completionStageRxInvoker = ClientBuilder
				.newClient()
				.target("http://localhost:8080/demo/lagjug/attendee")
				.request()
				.rx(); // <-- this was in JAX-RS 2.1
				

		CompletionStage<List<Attendee>> firstSet = completionStageRxInvoker.get(new AttendeeGenericType());
		
		CompletionStage<List<Attendee>> secondSet = ClientBuilder
				.newClient()
				.target("http://localhost:8080/demo/lagjug/attendee")
				.request()
				.rx()
				.get(new AttendeeGenericType());
		
//		bothSets is a completionStage that will return after the results of the first and secondSet Async call returns and
//		are combined

		CompletionStage<List<Attendee>> bothSets = firstSet.thenCombine(secondSet, 
				(firstAttendants, secondAttendants) -> {
					firstAttendants.addAll(secondAttendants);
					return firstAttendants;
				});

//		bothSets can be returned directly, but we may decide to put a check for any exception thrown during execution
//		This just shows how exception that occurs while making any of the calls can be handle in a clean way
		CompletionStage<Response> exceptionHandledNicelyResponse = bothSets.handle( (bothAttendants, e) -> {
			if(e != null){
				logger.log(Level.SEVERE, "An error occured combining both results", e);
				return Response
						.serverError()
						.entity(new ResponseMessage("we are truly sorry"))
						.build();
			} else {
				return Response.ok(bothAttendants).build();
			}
		} );

		return exceptionHandledNicelyResponse;
	}	
	
	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Error", e);
		} 
	}
	
}
