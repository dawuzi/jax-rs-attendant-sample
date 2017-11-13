package com.dawuzi.service;

import java.net.URI;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.dawuzi.db.MockDatabase;
import com.dawuzi.model.Attendee;
import com.dawuzi.model.ResponseMessage;

/**
 * @author DAWUZI
 *
 */

@Stateless
public class AttendeeService {

	@Inject
	private MockDatabase mockDatabase;
	
	public List<Attendee> getAllAttendees() {
		return mockDatabase.findAll();
	}
	
	public Response getAttendee(Long id) {
		
		Attendee attendee = mockDatabase.findById(id);
		
//		As shown below, a status of not found (404) was used in building the response.
//		Other common HTTP responses can be set in a similar manner
		if(attendee == null){
			return Response
					.status(Status.NOT_FOUND) 
					.entity(new ResponseMessage("No attendee found with id : "+id))
					.build();
		}
		
//		Response.ok is just one of the numerous methods that returns a builder with common HTTP status codes
		return Response.ok(attendee).build();
	}

	public Response createAttendee(Attendee attendee, UriInfo uriInfo) {
		
		if(attendee.getId() != null){
			return Response
					.status(Status.BAD_REQUEST)
					.entity(new ResponseMessage("Cannot create an attendee with an id specified"))
					.build();
		}
		
		mockDatabase.create(attendee);
		
		URI uri = null;
		
		if(uriInfo != null){
			uri = uriInfo.getAbsolutePathBuilder().path(attendee.getId().toString()).build();
		}
		
		return Response.created(uri).build();
	}

	public Response updateAttendee(Attendee attendee) {
		
		if(attendee.getId() == null){
			return Response
					.status(Status.BAD_REQUEST)
					.entity(new ResponseMessage("Cannot update an attendee without an id specified"))
					.build();
		}
		
		Long id = attendee.getId();
		
		Attendee jugAttendee = mockDatabase.findById(id);
		
		if(jugAttendee == null){
			return Response
					.status(Status.NOT_FOUND)
					.entity(new ResponseMessage("No attendee found with id : "+id))
					.build();
		}
		
		mockDatabase.update(attendee);
		
		return Response.ok(new ResponseMessage("update successful")).build();
	}

	public int getAttendeeCount() {
		return mockDatabase.findAll().size();
	}
}
