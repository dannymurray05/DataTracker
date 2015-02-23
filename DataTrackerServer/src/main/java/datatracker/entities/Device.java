package datatracker.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Device {

	@Id
	private String phoneNumber;

	@ManyToOne
	private User user;

	
	
}
