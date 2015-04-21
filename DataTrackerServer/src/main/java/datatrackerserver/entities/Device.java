package datatrackerserver.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Device {

	@Id
	private String phoneNumber;

	@ManyToOne
	private User user;

	private boolean autoShutoff;
	
	private long quota;
	
	private long threshold;

	/**
	 * To be used when requesting user. Email with this code will be sent to user and when they confirm it,
	 * the device will become under that user's control.
	 */
	@JsonIgnore
	private String validationCode;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean getAutoShutoff() {
		return autoShutoff;
	}

	public void setAutoShutoff(boolean autoShutoff) {
		this.autoShutoff = autoShutoff;
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

	public String getValidationCode() {
		return validationCode;
	}

	public void setValidationCode(String validationCode) {
		this.validationCode = validationCode;
	}	

	/**
	 * For first creating device when user has not confirmed device
	 * @param phoneNumber
	 */
	public Device(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * For when creating for a newly signed up user
	 * @param phoneNumber
	 * @param user
	 */
	public Device(String phoneNumber, User user) {
		this.phoneNumber = phoneNumber;
		this.user = user;
	}
	
	protected Device() {}
}
