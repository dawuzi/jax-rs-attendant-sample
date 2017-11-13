package com.dawuzi.model;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author DAWUZI
 *
 */

@Getter
@Setter
@EqualsAndHashCode(of="id")
@ToString
@XmlRootElement(name="LagosJugAttendant")
@XmlAccessorType(XmlAccessType.FIELD)
public class Attendee implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4975469142358176841L;
	
	@Positive(message = "id must be positive")
	private Long id;
	
	@NotBlank(message="first name must be specified")
	private String firstName;
	
	@NotBlank(message="last name must be specified")
	private String lastName;

	@Email(message="invalid email specified")
	@NotBlank(message = "email must be specified")
	private String email;
	
	@NotBlank(message = "job title must be specified")
	private String jobTitle;
	
	private String githubUrl;
	
	private String reason;

}
