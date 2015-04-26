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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import datatrackerserver.entities.Device;
import datatrackerserver.entities.UsageHistory;
import datatrackerserver.entities.User;
import datatrackerserver.entitymanagement.DataHandler;
import datatrackerserver.entitymanagement.DeviceHandler;
import datatrackerserver.entitymanagement.DeviceHandler.DeviceSettings;
import datatrackerserver.entitymanagement.UserHandler;
import datatrackerserver.entitymanagement.UserHandler.UserSettings;

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
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleException(Exception e) {
    	System.out.print(e.getMessage());
    	return e.getMessage();
    }

	@RequestMapping(value = "/register_user", method = RequestMethod.POST)
	public ResponseEntity<String> registerUser(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password, @RequestParam(value = "email") String email) {
		UserHandler.RegistrationError error = appContext.getBean(UserHandler.class).registerUser(phoneNumber, password, email);
		
		if(error != null) {
			return new ResponseEntity<String>(error.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>(HttpStatus.CREATED);
	}

	@RequestMapping(value = "/validate_email", method = RequestMethod.GET)
	public ResponseEntity<String> validateEmail(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "code") String code) {
		boolean validated = appContext.getBean(UserHandler.class).validateEmail(phoneNumber, code);
		
		if(validated) {
			return new ResponseEntity<String>("Email validated", HttpStatus.OK);
		}

		return new ResponseEntity<String>("Invalid code", HttpStatus.UNAUTHORIZED);
	}

	@RequestMapping(value = "/register_device", method = RequestMethod.POST)
	public ResponseEntity<String> registerDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "userPhoneNumber") String userPhoneNumber) {
		boolean deviceCreated = appContext.getBean(DeviceHandler.class).registerDevice(phoneNumber, userPhoneNumber);
		
		if(deviceCreated) {
			return new ResponseEntity<String>("Device created and request sent to user", HttpStatus.CREATED);
		}
		else {
			return new ResponseEntity<String>("New user request sent", HttpStatus.CREATED);
		}
	}

	@RequestMapping(value = "/validate_device", method = RequestMethod.GET)
	public ResponseEntity<String> validateDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "userPhoneNumber") String userPhoneNumber,
			@RequestParam(value = "code") String code) {
		boolean validated = appContext.getBean(DeviceHandler.class).validateDevice(phoneNumber, userPhoneNumber, code);
		
		if(validated) {
			return new ResponseEntity<String>("Device validated", HttpStatus.OK);
		}
		else {
			return new ResponseEntity<String>("Invalid code", HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = "/log_data", method = RequestMethod.POST)
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

    @RequestMapping(value = "/request_device_data", method = RequestMethod.GET)
    public @ResponseBody List<UsageHistory> requestDeviceData(@RequestParam(value="phoneNumber") String phoneNumber,
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
    
    @RequestMapping(value = "/request_user_data", method = RequestMethod.GET)
    public @ResponseBody List<UsageHistory> requestUserData(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password,
    		@RequestParam(value="beginDate") String beginDateStr, @RequestParam(value="endDate") String endDateStr,
    		HttpServletResponse response) {
		Date beginDate = DataTrackerConstants.stringToDate(beginDateStr);
		Date endDate = DataTrackerConstants.stringToDate(endDateStr);
		if(beginDate == null || endDate == null) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		if(appContext.getBean(UserHandler.class).validateUserAndPassword(phoneNumber, password) == null) {
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		
        List<UsageHistory> usageHistory = appContext.getBean(DataHandler.class).getUserUsageData(phoneNumber, beginDate, endDate);
        
        if(usageHistory == null) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return usageHistory;
    }

    @RequestMapping(value = "/request_device_settings", method = RequestMethod.GET)
    public Device requestDeviceSettings(@RequestParam(value="phoneNumber") String phoneNumber) {
        Device device = appContext.getBean(DeviceHandler.class).getDeviceSettings(phoneNumber);

        return device;
    }

    
    @RequestMapping(value = "/request_user_settings", method = RequestMethod.GET)
    public User requestUserSettings(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password) {
    	User user = appContext.getBean(UserHandler.class).getUserSettings(phoneNumber, password);

        return user;
    }

    @RequestMapping(value = "/update_device_setting", method = RequestMethod.POST)
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
    
    @RequestMapping(value = "/update_user_setting", method = RequestMethod.POST)
    public ResponseEntity<String> updateUserSetting(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password,
    		@RequestParam(value="setting") String settingStr, @RequestParam(value="value") String value) {
    	UserSettings setting = UserSettings.valueOf(settingStr);
    	if(setting == null) {
    		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    	}

        boolean success = appContext.getBean(UserHandler.class).setUserSetting(phoneNumber, password, setting, value);

        if(success) {
        	return new ResponseEntity<String>(HttpStatus.OK);
        }
        else {
        	return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }
    
	/*@RequestMapping("/login")
	public String userLogin(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password) {
		return RegistrationHandler.INSTANCE.registerUser(phoneNumber, password).toString();
	}*/
}