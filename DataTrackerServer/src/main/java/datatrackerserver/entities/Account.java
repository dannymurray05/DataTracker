package datatrackerserver.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import datatrackerserver.security.SecurityManager;

@Entity
public class Account {
	
	@Id
	private String phoneNumber;

	@JsonIgnore
	private String email;
	
	@JsonIgnore
	private boolean emailValidated;

	@JsonIgnore
	private String password;

	@JsonIgnore
	private String validationCode;
	
	private long quota;
	
	private long threshold;
	
	private int billingCycleLength;

	@OneToMany(mappedBy = "account")
	private Set<Device> devices = new HashSet<>();

	protected Account() {}
	
	public Account(String phoneNumber, String password, String email, long quota, long threshold) {
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.quota = quota;
		this.threshold = threshold;
		this.email = email;
		this.validationCode = SecurityManager.generateRandomCode();
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

	public int getBillingCycleLength() {
		return billingCycleLength;
	}

	public void setBillingCycleLength(int billingCycleLength) {
		this.billingCycleLength = billingCycleLength;
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
	
	public void setValidationCode(String code) {
		validationCode = code;
	}

	@Override
	public String toString() {
		return String.format("Account[Phone Number='%s', Password='%s', Quota=%d, Threshold=%d, Email='%s']",
				phoneNumber, password, quota, threshold, email);
	}
}
