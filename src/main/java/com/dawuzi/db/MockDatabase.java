package com.dawuzi.db;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

import com.dawuzi.model.Attendee;

/**
 * 
 * A singleton class to expose DB like operations for the {@link Attendee} object
 * 
 * @author DAWUZI
 *
 */

@Singleton
public class MockDatabase {
	
	private Map<Long, Attendee> localDB = new ConcurrentHashMap<>();
	
	@PostConstruct
	public void init(){
		for(int x=1; x<=5; x++){
			Attendee attendant = new Attendee();
			
			attendant.setEmail("test-"+x+"@test.com");
			attendant.setFirstName("TestFirstName-"+x);
			attendant.setJobTitle("Java Ninja");
			attendant.setLastName("TestLastName-"+x);
			attendant.setReason("To connect with other awesome Java ninjas");
			
			create(attendant);
		}
	}
	
	public boolean create(Attendee lagosJugAttendant){
		
		lagosJugAttendant.setId(getNextId());
		
		localDB.put(lagosJugAttendant.getId(), lagosJugAttendant);
		
		return true;
	}
	
	public boolean update(Attendee lagosJugAttendant){
		
		if(lagosJugAttendant.getId() == null){
			return false;
		}
		
		Attendee attendant = findById(lagosJugAttendant.getId());
		
		if(attendant == null){
			return false;
		}
		
		localDB.put(lagosJugAttendant.getId(), attendant);
		
		return true;
	}
	
	@Lock(LockType.READ)
	public Attendee findById(Long id){
		return localDB.get(id);
	}
	
	@Lock(LockType.READ)
	public List<Attendee> findAll(){
		return localDB.values().stream().collect(Collectors.toList());
	}
	
	private Long getNextId(){
		return localDB.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
	}
}
