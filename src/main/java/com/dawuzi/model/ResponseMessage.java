package com.dawuzi.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement(name = "ResponseMessage")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2394869376599810580L;
	
	private String description;

	public ResponseMessage() {
	}
	public ResponseMessage(String description) {
		this.description = description;
	}
	
}
