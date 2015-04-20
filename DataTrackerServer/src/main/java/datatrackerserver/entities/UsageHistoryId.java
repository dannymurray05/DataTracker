package datatrackerserver.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class UsageHistoryId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3387762367276300030L;

	private String phoneNumber;
	private Date date;

	protected UsageHistoryId() {}

	public UsageHistoryId(String phoneNumber, Date date) {
		this.phoneNumber = phoneNumber;
		this.date = date;
	}

	public String getPhoneNumber() { return phoneNumber; }
	public Date getDate() { return date; }

	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
	public void setDate(Date date) { this.date = date; }
	
}
