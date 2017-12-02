package com.dawuzi.model;

import java.io.Serializable;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author DAWUZI
 *
 */

@Getter
@Setter
@ToString
public class AttendeeFilterParam implements Serializable {

	private static final long serialVersionUID = -8314130100754491109L;

	@DefaultValue(value="0") 
	@PositiveOrZero(message = "start must not be negative") 
	@QueryParam("start") 
	private int start;
	
	@DefaultValue(value="10") 
	@Positive(message = "size must be greater than zero") 
	@QueryParam("size") 
	private int size;
	
	@DefaultValue(value="id") 
	@QueryParam("order") 
	private String order;
		
}
