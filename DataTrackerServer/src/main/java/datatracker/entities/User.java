package datatracker.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class User {
	
	@Id
	private String phoneNumber;

	private String email;

	@JsonIgnore
	private String password;
	
	private long quota;
	
	private long threshold;

	@OneToMany(mappedBy = "user")
	private Set<Device> devices = new HashSet<>();

	protected User() {}
	
	public User(String phoneNumber, String password, String email, long quota, long threshold) {
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.quota = quota;
		this.threshold = threshold;
		this.email = email;
	}

	@Override
	public String toString() {
		return String.format("User[Phone Number='%s', Password='%s', Quota=%d, Threshold=%d, Email='%s']",
				phoneNumber, password, quota, threshold, email);
	}
}
