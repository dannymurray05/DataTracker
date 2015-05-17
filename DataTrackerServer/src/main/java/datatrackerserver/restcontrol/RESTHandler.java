package datatrackerserver.restcontrol;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import datatrackerserver.entities.Account;
import datatrackerserver.entities.Device;
import datatrackerserver.entities.UsageHistory;
import datatrackerserver.entitymanagement.AccountHandler;
import datatrackerserver.entitymanagement.DataHandler;
import datatrackerserver.entitymanagement.DeviceHandler;
import datatrackerstandards.AccountRegistrationStatus;
import datatrackerstandards.AccountValidationStatus;
import datatrackerstandards.DataError;
import datatrackerstandards.DataTrackerConstants;
import datatrackerstandards.DeviceRegistrationStatus;
import datatrackerstandards.DeviceValidationStatus;
import datatrackerstandards.settings.AccountSetting;
import datatrackerstandards.settings.DeviceSetting;

@Component
@RestController
public class RESTHandler {
	@Autowired
	private ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(Exception e) {
    	System.out.println(e.getMessage());
    	return e.getMessage();
    }

	@RequestMapping(value = "/register_account")
    @ResponseBody 
	public ResponseEntity<String> registerAccount(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password, @RequestParam(value = "email") String email) {
		AccountRegistrationStatus status = appContext.getBean(AccountHandler.class).registerAccount(phoneNumber, password, email);
       	System.out.println("Account Registration Status: " + status.getStatusMessage());
		return new ResponseEntity<String>(status.name(), status.getSuccess() ? HttpStatus.CREATED : HttpStatus.NOT_ACCEPTABLE);
	}

	@RequestMapping(value = "/validate_email")
    @ResponseBody 
	public ResponseEntity<String> validateEmail(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "code") String code) {
		boolean validated = appContext.getBean(AccountHandler.class).validateEmail(phoneNumber, code);
		
		if(validated) {
			System.out.println("Email validated");
			return new ResponseEntity<String>("Email validated", HttpStatus.OK);
		}

		System.out.println("Invalid account validation code");
		return new ResponseEntity<String>("Invalid account validation code", HttpStatus.UNAUTHORIZED);
	}

	@RequestMapping(value = "/register_device")
    @ResponseBody 
	public ResponseEntity<String> registerDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "accountPhoneNumber") String accountPhoneNumber) {
		DeviceHandler deviceHandler = appContext.getBean(DeviceHandler.class);
		DeviceRegistrationStatus status = deviceHandler.registerDevice(phoneNumber, accountPhoneNumber);
       	System.out.println("Device Registration Status: " + status.getStatusMessage());
       	//Device device = deviceHandler.getDeviceSettings(accountPhoneNumber);
		return new ResponseEntity<String>(status.name(), status.getSuccess() ? HttpStatus.CREATED : HttpStatus.NOT_ACCEPTABLE);
	}

	@RequestMapping(value = "/validate_device")
    @ResponseBody 
	public ResponseEntity<String> validateDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "accountPhoneNumber") String accountPhoneNumber,
			@RequestParam(value = "code") String code) {
		boolean validated = appContext.getBean(DeviceHandler.class).validateDevice(phoneNumber, accountPhoneNumber, code);
		
		if(validated) {
			System.out.println("Device validated");
			return new ResponseEntity<String>("Device validated", HttpStatus.OK);
		}
		else {
			System.out.println("Invalid device validation code");
			return new ResponseEntity<String>("Invalid device validation code", HttpStatus.UNAUTHORIZED);
		}
	}
	
	@RequestMapping(value = "/remove_device")
    @ResponseBody 
	public ResponseEntity<String> removeDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "accountPhoneNumber") String accountPhoneNumber,
			@RequestParam(value = "password") String password) {
		if(appContext.getBean(AccountHandler.class).validateAccountAndPassword(accountPhoneNumber, password) == null) {
			System.out.println("Invalid account number or password for device removal");
			return new ResponseEntity<String>("Invalid account number or password", HttpStatus.UNAUTHORIZED);
		}

		boolean removed = appContext.getBean(AccountHandler.class).removeDevice(phoneNumber, accountPhoneNumber);
		
		if(removed) {
			System.out.println("Device removed");
			return new ResponseEntity<String>("Device removed", HttpStatus.OK);
		}
		else {
			System.out.println("Device removal failed");
			return new ResponseEntity<String>("Device removal failed", HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = "/log_data")
    @ResponseBody 
	public ResponseEntity<String> logData(@RequestParam(value="phoneNumber") String phoneNumber, @RequestParam(value="date") String dateStr,
			@RequestParam(value="hour") String hour, @RequestParam(value="bytes") String bytes) {
		Date date = DataTrackerConstants.stringToDate(dateStr);
		if(date == null) {
			System.out.println(DataError.INVALID_DATE_FORMAT.getErrorMessage());
			return new ResponseEntity<String>(DataError.INVALID_DATE_FORMAT.name(), HttpStatus.NOT_ACCEPTABLE);
		}

		DataError error = appContext.getBean(DataHandler.class).logData(phoneNumber, date, Integer.valueOf(hour), Integer.valueOf(bytes));

		if(error != null) {
			System.out.println(error.getErrorMessage());
			return new ResponseEntity<String>(error.name(), HttpStatus.ALREADY_REPORTED);
		}

		System.out.printf("Data Logged: [Phone Number: %s, Date: %s, Hour: %s, Bytes: %s]\n",  phoneNumber, dateStr, hour, bytes);
		return new ResponseEntity<String>(HttpStatus.CREATED);
	}

    @RequestMapping(value = "/request_device_data")
    @ResponseBody 
    public List<UsageHistory> requestDeviceData(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="beginDate") String beginDateStr, @RequestParam(value="endDate") String endDateStr,
    		HttpServletResponse response) {
		Date beginDate = DataTrackerConstants.stringToDate(beginDateStr);
		Date endDate = DataTrackerConstants.stringToDate(endDateStr);
		if(beginDate == null || endDate == null) {
			System.out.println(DataError.INVALID_DATE_FORMAT.getErrorMessage());
        	response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return null;
		}
		
        List<UsageHistory> usageHistory = appContext.getBean(DataHandler.class).getUsageData(phoneNumber, beginDate, endDate);
        
        if(usageHistory == null) {
			System.out.println("Requested device data not found!");
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

		System.out.println("Requested device data found!");
        return usageHistory;
    }
    
    @RequestMapping(value = "/request_account_data")
    @ResponseBody
    public List<UsageHistory> requestAccountData(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="beginDate") String beginDateStr, @RequestParam(value="endDate") String endDateStr,
    		HttpServletResponse response) {
		Date beginDate = DataTrackerConstants.stringToDate(beginDateStr);
		Date endDate = DataTrackerConstants.stringToDate(endDateStr);
		if(beginDate == null || endDate == null) {
			System.out.println(DataError.INVALID_DATE_FORMAT.getErrorMessage());
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
        List<UsageHistory> usageHistory = appContext.getBean(DataHandler.class).getAccountUsageData(phoneNumber, beginDate, endDate);
        
        if(usageHistory == null) {
        	System.out.println("Requested account data not found!");
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        System.out.println("Requested account data found!");
        return usageHistory;
    }

    @RequestMapping(value = "/request_device_settings")
    @ResponseBody
    public Device requestDeviceSettings(@RequestParam(value="phoneNumber") String phoneNumber,
    		HttpServletResponse response) {
        Device device = appContext.getBean(DeviceHandler.class).getDeviceSettings(phoneNumber);

    	if(device != null) {
        	System.out.println("Requested device settings found!");
    		response.setStatus(HttpServletResponse.SC_OK);
    		return device;
    	}
    	else {
    		System.out.println("Requested device settings not found!");
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		return null;
    	}
    }

    @RequestMapping(value = "/request_account_settings")
    @ResponseBody
    public Account requestAccountSettings(@RequestParam(value="phoneNumber") String phoneNumber,
    		HttpServletResponse response) {
    	Account account = appContext.getBean(AccountHandler.class).getAccountSettings(phoneNumber);

    	if(account != null) {
    		System.out.println("Requested account settings found!");
    		response.setStatus(HttpServletResponse.SC_OK);
    		return account;
    	}
    	else {
    		System.out.println("Requested account settings not found!");
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		return null;
    	}
    }

    @RequestMapping(value = "/update_device_setting")
    @ResponseBody
    public ResponseEntity<String> updateDeviceSetting(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="setting") String settingStr, @RequestParam(value="value") String value) {
    	DeviceSetting setting = null;
    	try {
    		setting = DeviceSetting.valueOf(settingStr);
    	}
    	catch(IllegalArgumentException e) {
    		System.out.println("Invalid setting for update device setting request");
    		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    	}

    	if(setting == null) {
    		System.out.println("Invalid setting for update device setting request");
    		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    	}

        boolean success = appContext.getBean(DeviceHandler.class).setDeviceSetting(phoneNumber, setting, value);

        if(success) {
    		System.out.println("Updated device setting for phone number: " + phoneNumber);
        	return new ResponseEntity<String>(HttpStatus.OK);
        }
        else {
    		System.out.println("Could not update device setting for phone number: " + phoneNumber);
        	return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
        }
    }
    
    @RequestMapping(value = "/update_account_setting")
    @ResponseBody
    public ResponseEntity<String> updateAccountSetting(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password,
    		@RequestParam(value="setting") String settingStr, @RequestParam(value="value") String value) {
    	AccountSetting setting = null;
    	try {
    		setting = AccountSetting.valueOf(settingStr);
    	}
    	catch(IllegalArgumentException e) {
    		System.out.println("Invalid setting for update account setting request");
    		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    	}	
    	
    	if(setting == null) {
    		System.out.println("Invalid setting for update account setting request");
    		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    	}

        boolean success = appContext.getBean(AccountHandler.class).setAccountSetting(phoneNumber, password, setting, value);

        if(success) {
    		System.out.println("Updated account setting for phone number: " + phoneNumber);
        	return new ResponseEntity<String>(HttpStatus.OK);
        }
        else {
    		System.out.println("Could not update account setting for phone number: " + phoneNumber);
        	return new ResponseEntity<String>(HttpStatus.NOT_ACCEPTABLE);
        }
    }
  
    @RequestMapping(value = "/valid_device")
    @ResponseBody
    public ResponseEntity<String> validDevice(@RequestParam(value="phoneNumber") String phoneNumber) {
    	DeviceHandler handler = appContext.getBean(DeviceHandler.class);
    	DeviceValidationStatus status = handler.validDevice(phoneNumber);
    	Account account = handler.getDeviceSettings(phoneNumber).getAccount();
    	String accountPhoneNumber = "";
    	if(account != null) {
    		accountPhoneNumber = account.getPhoneNumber();
    	}
       	System.out.println("Device Validation Status: " + status.getStatusMessage());
       	return new ResponseEntity<String>(status.name() + ":" + accountPhoneNumber,
       			status.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_ACCEPTABLE);
    }

    @RequestMapping(value = "/valid_account")
    public @ResponseBody ResponseEntity<String> validAccount(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password) {
		AccountValidationStatus status = appContext.getBean(AccountHandler.class)
				.validAccount(phoneNumber, password);
       	System.out.println("Account Validation Status: " + status.getStatusMessage());
       	return new ResponseEntity<String>(status.name(), status.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_ACCEPTABLE);
    }
}
