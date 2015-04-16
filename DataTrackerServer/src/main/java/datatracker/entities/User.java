package datatracker.entities;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class User {

	@JsonIgnore
	private static SecureRandom random = new SecureRandom();
	
	@Id
	private String phoneNumber;

	private String email;
	
	private boolean emailValidated;

	@JsonIgnore
	private String password;

	@JsonIgnore
	private String validationCode;
	
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
		this.validationCode = generateRandomCode();
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean getEmailValidated() {
		return emailValidated;
	}

	public void setEmailValidated(boolean validated) {
		emailValidated = validated;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getQuota() {
		return quota;
	}

	public void setQuota(long quota) {
		this.quota = quota;
	}

	public long getThreshold() {
		return threshold;
	}

	public void setThreshold(long threshold) {
		this.threshold = threshold;
	}

	public Set<Device> getDevices() {
		return devices;
	}

	public void setDevices(Set<Device> devices) {
		this.devices = devices;
	}
	
	public String getValidationCode() {
		return this.validationCode;
	}
	
	public void getValidationCode(String code) {
		validationCode = code;
	}

	@Override
	public String toString() {
		return String.format("User[Phone Number='%s', Password='%s', Quota=%d, Threshold=%d, Email='%s']",
				phoneNumber, password, quota, threshold, email);
	}

	public static String generateRandomCode() {
		return new BigInteger(130, random).toString(32);
	}
}
