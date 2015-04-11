package datatracker.datamangement;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatracker.entities.UsageHistory;
import datatracker.entities.UsageHistoryId;
import datatracker.repositories.UsageHistoryRepository;

@Configuration
@EnableAutoConfiguration
public class DataHandler {
	public static final DataHandler INSTANCE = new DataHandler();
	
	@Autowired
	private ApplicationContext appContext;
	
	//public static final long 
	
	public static enum DataError {
		INVALID_DATA("Invalid data for log request."),
		;

		public final String errorMessage;

		DataError(String message) {
			errorMessage = message;
		}
		
		public String getErrorMessage() {
			return errorMessage;
		}
	}

	public DataError logData(String phoneNumber, Date date, int hour, int bytes) {
		UsageHistoryRepository usageHistoryRepo = appContext.getBean(UsageHistoryRepository.class);
		
		Iterable<UsageHistory> completeUsageHistory = usageHistoryRepo.findAll();
		for(UsageHistory usageHistory : completeUsageHistory) {
			System.out.println(usageHistory.toString());
		}	
		
		UsageHistoryId id = new UsageHistoryId(phoneNumber, date);
		UsageHistory record = usageHistoryRepo.findOne(id);

		if(record == null) {
			record = new UsageHistory(phoneNumber, date);
		}

		if(!record.insertData(hour, bytes)) {
			return DataError.INVALID_DATA;
		}

		usageHistoryRepo.save(record);
		
		return null; //success
	}
}
