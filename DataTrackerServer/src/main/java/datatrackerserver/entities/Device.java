package datatrackerserver.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import datatrackerstandards.settings.DeviceSetting;

@Entity
public class Device {

	@Id
	private String phoneNumber;

	@JsonIgnore
	@ManyToOne
	private Account account;

	private boolean autoShutoff;
	
	private long quota;
	
	private long threshold;

	/**
	 * To be used when requesting account membership.
	 * An email with this code will be sent to account and when they confirm it,
	 * the device will become under that account's control.
	 */
	@JsonIgnore
	private String validationCode;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
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
	 * For first creating device when account has not confirmed device
	 * @param phoneNumber
	 */
	public Device(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		setDefaults();
	}

	/**
	 * For when creating for a newly signed up account
	 * @param phoneNumber
	 * @param account
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public Device(String phoneNumber, Account account) {
		this.phoneNumber = phoneNumber;
		this.account = account;
		setDefaults();
	}

	protected Device() {
		setDefaults();
	}

	private void setDefaults() {
		try {
			for(DeviceSetting setting : DeviceSetting.values()) {
				setting.getSettingField().set(this, setting.getDefaultValue());
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
