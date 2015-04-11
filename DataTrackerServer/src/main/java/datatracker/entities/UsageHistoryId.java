package datatracker.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class UsageHistoryId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3387762367276300030L;

	String phoneNumber;
	Date date;
	
	public UsageHistoryId(String phoneNumber, Date date) {
		this.phoneNumber = phoneNumber;
		this.date = date;
	}
}
