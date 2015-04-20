package datatrackerserver.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity @IdClass(UsageHistoryId.class)
public class UsageHistory {

	/*@EmbeddedId
	private UsageHistoryId id;*/
	@Id
	private String phoneNumber;
	
	@Id
	private Date date;
	
	int usage[]; //one element for each hour of the day

	protected UsageHistory() {}
	
	public UsageHistory(UsageHistoryId id) {
		this(id.getPhoneNumber(), id.getDate());
	}

	public UsageHistory(String phoneNumber, Date date) {
		this.phoneNumber = phoneNumber;
		this.date = date;
		usage = new int[24];
		for(int i = 0; i < 24; i++) {
			usage[i] = -1;
		}
	}

	public boolean insertData(int hour, int bytes) {
		if(hour < 0 || hour > 23) { //out of range
			return false;
		}
		else if(usage[hour] >= 0) { //hour already recorded
			return false;
		}
		else if(bytes < 0) { //bytes cannot be negative
			return false;
		}
		else {
			usage[hour] = bytes;
			return true;
		}
	}


	public String getPhoneNumber() { return phoneNumber; }
	public Date getDate() { return date; }
	public int[] getUsageData() { return usage; }

	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
	public void setDate(Date date) { this.date = date; }

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(String.format("UsageData[Phone Number='%s', Date='%s', Usage='(",
				phoneNumber, date.toString()));
		for(int i = 0; i < 23; i++) {
			result.append(usage[i] + ", ");
		}
		result.append(usage[23] + ")]");

		return result.toString();
	}
}