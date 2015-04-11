package datatracker.entities;

import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.IdClass;



@Entity @IdClass(UsageHistoryId.class)
public class UsageHistory {

	@EmbeddedId
	private UsageHistoryId id;
	
	int usage[]; //one element for each hour of the day

	protected UsageHistory() {}
	
	public UsageHistory(String phoneNumber, Date date) {
		id.phoneNumber = phoneNumber;
		id.date = date;
		usage = new int[24];
		for(int i = 0; i < 24; i++) {
			usage[i] = 0;
		}
	}

	public boolean insertData(int hour, int bytes) {
		if(hour < 0 || hour > 23) { //out of range
			return false;
		}
		else if(usage[hour] > 0) { //hour already recorded
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

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(String.format("UsageData[Phone Number='%s', Date='%s', Usage='(",
				id.phoneNumber, id.date.toString()));
		for(int i = 0; i < 24; i++) {
			result.append(usage[i] + ", ");
		}
		result.append(")]");

		return result.toString();
	}
}