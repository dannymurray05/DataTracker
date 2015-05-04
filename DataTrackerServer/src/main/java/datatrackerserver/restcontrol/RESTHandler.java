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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import datatrackerserver.entities.Account;
import datatrackerserver.entities.Device;
import datatrackerserver.entities.UsageHistory;
import datatrackerserver.entitymanagement.AccountHandler;
import datatrackerserver.entitymanagement.DataHandler;
import datatrackerserver.entitymanagement.DeviceHandler;
import datatrackerstandards.AccountRegistrationStatus;
import datatrackerstandards.AccountSettings;
import datatrackerstandards.AccountValidationStatus;
import datatrackerstandards.DataTrackerConstants;
import datatrackerstandards.DeviceRegistrationStatus;
import datatrackerstandards.DeviceSettings;
import datatrackerstandards.DeviceValidationStatus;

@Component
@RestController
public class RESTHandler {
	@Autowired
	private ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

    /*@ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleException(Exception e) {
    	System.out.println(e.getMessage());
    	return e.getMessage();
    }*/

	@RequestMapping(value = "/register_account")
    @ResponseBody 
	public ResponseEntity<String> registerAccount(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password, @RequestParam(value = "email") String email) {
		AccountRegistrationStatus status = appContext.getBean(AccountHandler.class).registerAccount(phoneNumber, password, email);
       	System.out.println(status.getStatusMessage());
		return new ResponseEntity<String>(status.name(), status.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/validate_email")
    @ResponseBody 
	public ResponseEntity<String> validateEmail(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "code") String code) {
		boolean validated = appContext.getBean(AccountHandler.class).validateEmail(phoneNumber, code);
		
		if(validated) {
			return new ResponseEntity<String>("Email validated", HttpStatus.OK);
		}

		return new ResponseEntity<String>("Invalid code", HttpStatus.UNAUTHORIZED);
	}

	@RequestMapping(value = "/register_device")
    @ResponseBody 
	public ResponseEntity<String> registerDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "accountPhoneNumber") String accountPhoneNumber) {
		DeviceRegistrationStatus status = appContext.getBean(DeviceHandler.class).registerDevice(phoneNumber, accountPhoneNumber);
       	System.out.println(status.getStatusMessage());
		return new ResponseEntity<String>(status.name(), status.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/validate_device")
    @ResponseBody 
	public ResponseEntity<String> validateDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "accountPhoneNumber") String accountPhoneNumber,
			@RequestParam(value = "code") String code) {
		boolean validated = appContext.getBean(DeviceHandler.class).validateDevice(phoneNumber, accountPhoneNumber, code);
		
		if(validated) {
			return new ResponseEntity<String>("Device validated", HttpStatus.OK);
		}
		else {
			return new ResponseEntity<String>("Invalid code", HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = "/log_data")
    @ResponseBody 
	public ResponseEntity<String> logData(@RequestParam(value="phoneNumber") String phoneNumber, @RequestParam(value="date") String dateStr,
			@RequestParam(value="hour") String hour, @RequestParam(value="bytes") String bytes) {
		Date date = DataTrackerConstants.stringToDate(dateStr);
		if(date == null) {
			return new ResponseEntity<String>("Invalid date format", HttpStatus.BAD_REQUEST);
		}

		DataHandler.DataError error = appContext.getBean(DataHandler.class).logData(phoneNumber, date, Integer.valueOf(hour), Integer.valueOf(bytes));

		if(error != null) {
			return new ResponseEntity<String>(error.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

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
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
        List<UsageHistory> usageHistory = appContext.getBean(DataHandler.class).getUsageData(phoneNumber, beginDate, endDate);
        
        if(usageHistory == null) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return usageHistory;
    }
    
    @RequestMapping(value = "/request_account_data")
    @ResponseBody
    public List<UsageHistory> requestAccountData(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password,
    		@RequestParam(value="beginDate") String beginDateStr, @RequestParam(value="endDate") String endDateStr,
    		HttpServletResponse response) {
		Date beginDate = DataTrackerConstants.stringToDate(beginDateStr);
		Date endDate = DataTrackerConstants.stringToDate(endDateStr);
		if(beginDate == null || endDate == null) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		if(appContext.getBean(AccountHandler.class).validateAccountAndPassword(phoneNumber, password) == null) {
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		
        List<UsageHistory> usageHistory = appContext.getBean(DataHandler.class).getAccountUsageData(phoneNumber, beginDate, endDate);
        
        if(usageHistory == null) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return usageHistory;
    }

    @RequestMapping(value = "/request_device_settings")
    @ResponseBody
    public Device requestDeviceSettings(@RequestParam(value="phoneNumber") String phoneNumber) {
        Device device = appContext.getBean(DeviceHandler.class).getDeviceSettings(phoneNumber);

        return device;
    }

    
    @RequestMapping(value = "/request_account_settings")
    @ResponseBody
    public Account requestAccountSettings(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password) {
    	Account account = appContext.getBean(AccountHandler.class).getAccountSettings(phoneNumber, password);

        return account;
    }

    @RequestMapping(value = "/update_device_setting")
    @ResponseBody
    public ResponseEntity<String> updateDeviceSetting(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="setting") String settingStr, @RequestParam(value="value") String value) {
    	DeviceSettings setting = DeviceSettings.valueOf(settingStr);
    	if(setting == null) {
    		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    	}

        boolean success = appContext.getBean(DeviceHandler.class).setDeviceSetting(phoneNumber, setting, value);

        if(success) {
        	return new ResponseEntity<String>(HttpStatus.OK);
        }
        else {
        	return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @RequestMapping(value = "/update_account_setting")
    @ResponseBody
    public ResponseEntity<String> updateAccountSetting(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password,
    		@RequestParam(value="setting") String settingStr, @RequestParam(value="value") String value) {
    	AccountSettings setting = AccountSettings.valueOf(settingStr);
    	if(setting == null) {
    		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    	}

        boolean success = appContext.getBean(AccountHandler.class).setAccountSetting(phoneNumber, password, setting, value);

        if(success) {
        	return new ResponseEntity<String>(HttpStatus.OK);
        }
        else {
        	return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }
  
    @RequestMapping(value = "/valid_device")
    @ResponseBody
    public ResponseEntity<String> validDevice(@RequestParam(value="phoneNumber") String phoneNumber) {
    	DeviceValidationStatus status = appContext.getBean(DeviceHandler.class).validDevice(phoneNumber);
       	System.out.println(status.getStatusMessage());
       	return new ResponseEntity<String>(status.name(), status.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/valid_account")
    public @ResponseBody ResponseEntity<String> validAccount(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password) {
		AccountValidationStatus status = appContext.getBean(AccountHandler.class)
				.validAccount(phoneNumber, password);
       	System.out.println(status.getStatusMessage());
       	return new ResponseEntity<String>(status.name(), status.getSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
