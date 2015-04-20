package datatrackerserver.entitymanagement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatrackerserver.entities.Device;
import datatrackerserver.entities.UsageHistory;
import datatrackerserver.entities.UsageHistoryId;
import datatrackerserver.entities.User;
import datatrackerserver.repositories.UsageHistoryRepository;
import datatrackerserver.repositories.UserRepository;

@Configuration
@EnableAutoConfiguration
public class DataHandler {
	public static final DataHandler INSTANCE = new DataHandler();
	
	@Autowired
	private ApplicationContext appContext;

	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}
	
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
		
		UsageHistoryId id = new UsageHistoryId(phoneNumber, date);
		UsageHistory record = usageHistoryRepo.findOne(id);

		if(record == null) {
			record = new UsageHistory(phoneNumber, date);
		}

		if(!record.insertData(hour, bytes)) {
			System.out.println(DataError.INVALID_DATA.getErrorMessage());
			return DataError.INVALID_DATA;
		}

		usageHistoryRepo.save(record);
		
		//debug
		Iterable<UsageHistory> completeUsageHistory = usageHistoryRepo.findAll();
		for(UsageHistory usageHistory : completeUsageHistory) {
			System.out.println(usageHistory.toString());
		}
		//end debug
		
		return null; //success
	}

	public List<UsageHistory> getUsageData(String phoneNumber, Date beginDate, Date endDate) {
		UsageHistoryRepository usageHistoryRepo = appContext.getBean(UsageHistoryRepository.class);
		
		List<UsageHistory> usageHistoryRange = usageHistoryRepo.findByDateBetween(beginDate, endDate);
		
		//debug
		for(UsageHistory uh : usageHistoryRange) {
			System.out.println(uh.toString());
		}
		//end debug

		return usageHistoryRange;
	}

	public List<UsageHistory> getUserUsageData(String phoneNumber, Date beginDate, Date endDate) {
		UserRepository userRepo = appContext.getBean(UserRepository.class);
		UsageHistoryRepository usageHistoryRepo = appContext.getBean(UsageHistoryRepository.class);
		
		User user = userRepo.findOne(phoneNumber);
		Set<Device> devices = user.getDevices();
		
		List<UsageHistory> usageHistoryRange = new ArrayList<UsageHistory>();
		for(Device device : devices) {
			String devicePhoneNumber = device.getPhoneNumber();
			usageHistoryRange.addAll(usageHistoryRepo.findByPhoneNumberAndDateBetween(devicePhoneNumber, beginDate, endDate));
		}
		
		//debug
		for(UsageHistory uh : usageHistoryRange) {
			System.out.println(uh.toString());
		}
		//end debug

		return usageHistoryRange;
	}
}
