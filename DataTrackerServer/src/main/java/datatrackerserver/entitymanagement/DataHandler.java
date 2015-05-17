package datatrackerserver.entitymanagement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatrackerserver.entities.Account;
import datatrackerserver.entities.Device;
import datatrackerserver.entities.UsageHistory;
import datatrackerserver.entities.UsageHistoryId;
import datatrackerserver.repositories.AccountRepository;
import datatrackerserver.repositories.UsageHistoryRepository;
import datatrackerstandards.DataError;

@Configuration
@EnableAutoConfiguration
public class DataHandler {
	@Autowired
	private ApplicationContext appContext;

	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}
	
	//public static final long 
	
	public DataError logData(String phoneNumber, Date date, int hour, int bytes) {
		UsageHistoryRepository usageHistoryRepo = appContext.getBean(UsageHistoryRepository.class);
		
		UsageHistoryId id = new UsageHistoryId(phoneNumber, date);
		UsageHistory record = usageHistoryRepo.findOne(id);

		if(record == null) {
			record = new UsageHistory(phoneNumber, date);
		}

		DataError error = record.insertData(hour, bytes);

		if(error != null) {
			return error;
		}

		usageHistoryRepo.save(record);
		
		//debug
		/*Iterable<UsageHistory> completeUsageHistory = usageHistoryRepo.findAll();
		for(UsageHistory usageHistory : completeUsageHistory) {
			System.out.println(usageHistory.toString());
		}*/
		//end debug
		
		return null; //success
	}

	public List<UsageHistory> getUsageData(String phoneNumber, Date beginDate, Date endDate) {
		UsageHistoryRepository usageHistoryRepo = appContext.getBean(UsageHistoryRepository.class);
		
		List<UsageHistory> usageHistoryRange = usageHistoryRepo.findByDateBetween(beginDate, endDate);
		
		//debug
		/*for(UsageHistory uh : usageHistoryRange) {
			System.out.println(uh.toString());
		}*/
		//end debug

		return usageHistoryRange;
	}

	public List<UsageHistory> getAccountUsageData(String phoneNumber, Date beginDate, Date endDate) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		UsageHistoryRepository usageHistoryRepo = appContext.getBean(UsageHistoryRepository.class);
		
		Account account = accountRepo.findOne(phoneNumber);
		Set<Device> devices = account.getDevices();
		
		List<UsageHistory> usageHistoryRange = new ArrayList<UsageHistory>();
		for(Device device : devices) {
			String devicePhoneNumber = device.getPhoneNumber();
			usageHistoryRange.addAll(usageHistoryRepo.findByPhoneNumberAndDateBetween(devicePhoneNumber, beginDate, endDate));
		}
		
		//debug
		/*for(UsageHistory uh : usageHistoryRange) {
			System.out.println(uh.toString());
		}*/
		//end debug

		return usageHistoryRange;
	}
}
