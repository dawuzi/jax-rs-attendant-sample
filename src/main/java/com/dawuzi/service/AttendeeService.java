package com.dawuzi.service;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
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

	public List<Attendee> getSomeAttendees(int start, int size, String order) {
		
		validateOrder(order); 
		
		List<Attendee> all = mockDatabase.findAll();
		
		if(all == null || all.isEmpty()){
			return all;
		}
		
//		the start location is more than the whole list so hence no match
		if(all.size() <= start){
			return Collections.emptyList();
		}
		
		sortAttendees(all, order);
		
		int endIndex = Math.min(start+size, all.size());
		
		return all.subList(start, endIndex);
	}
	
	private void sortAttendees(List<Attendee> all, String order) {
		
		if(order == null || order.isEmpty()){
			return;
		}
		all.sort((a1, a2) -> {
			
			if(a1 == null || a2 == null){
				return nullCompare(a1, a2);
			}
			
			Field attendeeField = getAttendeeField(order);
			
			Object val1 = getFieldValue(attendeeField, a1);
			Object val2 = getFieldValue(attendeeField, a2);
			
			if(val1 == null || val2 == null){
				return nullCompare(val1, val2); 
			}
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			int compareTo = ((Comparable)val1).compareTo(val2);
			
			return compareTo;
		});
	}

	private int nullCompare(Object val1, Object val2) {
		if(val1 == null && val2 == null){
			return 0;
		}
		if(val1 == null){
			return -1;
		}
		if(val2 == null){
			return 1;
		}
		throw new IllegalArgumentException("at least one must be null");
	}

	private Object getFieldValue(Field field, Object obj) {
		
		try {
			return new PropertyDescriptor(field.getName(), field.getDeclaringClass())
					.getReadMethod()
					.invoke(obj);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	private Field getAttendeeField(String field) {
		try {
			return Attendee.class.getDeclaredField(field);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	private void validateOrder(String order) {
		
		if(order != null && !order.isEmpty() && !isValidAttendeeField(order)){

			String validFields = getValidAttendeeFields()
					.stream()
					.collect(Collectors.joining(", "));
			
			String errorDescription = "Invalid order specified. Valid orders are "+validFields;
			
			Response response = Response.status(Status.BAD_REQUEST).entity(new ResponseMessage(errorDescription)).build();
			
			throw new WebApplicationException(response);
		}
	}

	private Set<String> getValidAttendeeFields() {
		
		Stream<Field> fields = Arrays.stream(Attendee.class.getDeclaredFields());
		
		Set<String> results = fields
				.map(f -> f.getName())
				.filter(name -> !name.equals("serialVersionUID"))
				.collect(Collectors.toSet());
		
		return results;
	}

	private boolean isValidAttendeeField(String order) {
		return getValidAttendeeFields().contains(order);
	}

}
