package datatrackerserver.restcontrol;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

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
import datatrackerserver.entitymanagement.UserHandler;
import datatrackerserver.entitymanagement.DeviceHandler.DeviceSettings;
import datatrackerserver.entitymanagement.UserHandler.UserSettings;

@Component
@RestController
public class RESTHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleException(Exception e) {
    	System.out.print(e.getMessage());
    	return e.getMessage();
    }
    
    
    public Date processDateStr(String dateStr) {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    	Date date = null;
		try {
			date = formatter.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return date;
		}
		
		return date;
    }

	@RequestMapping(value = "/new_user", method = RequestMethod.POST)
	public ResponseEntity<String> registerUser(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password, @RequestParam(value = "email") String email) {
		UserHandler.RegistrationError error = UserHandler.INSTANCE.registerUser(phoneNumber, password, email);
		
		if(error != null) {
			return new ResponseEntity<String>(error.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>(HttpStatus.CREATED);
	}

	@RequestMapping(value = "/register_device", method = RequestMethod.POST)
	public ResponseEntity<String> registerDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "userPhoneNumber") String userPhoneNumber) {
		boolean deviceCreated = DeviceHandler.INSTANCE.registerDevice(phoneNumber, userPhoneNumber);
		
		if(deviceCreated) {
			return new ResponseEntity<String>("Device created and request sent to user", HttpStatus.CREATED);
		}
		else {
			return new ResponseEntity<String>("New user request sent", HttpStatus.CREATED);
		}
	}

	@RequestMapping(value = "/validate_device", method = RequestMethod.POST)
	public ResponseEntity<String> registerDevice(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "userPhoneNumber") String userPhoneNumber,
			@RequestParam(value = "code") String code) {
		boolean validated = DeviceHandler.INSTANCE.validateDevice(phoneNumber, userPhoneNumber, code);
		
		if(validated) {
			return new ResponseEntity<String>("Device validated", HttpStatus.OK);
		}
		else {
			return new ResponseEntity<String>("Invalide validation code", HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = "/log_data", method = RequestMethod.POST)
	public ResponseEntity<String> logData(@RequestParam(value="phoneNumber") String phoneNumber, @RequestParam(value="date") String dateStr,
			@RequestParam(value="hour") String hour, @RequestParam(value="bytes") String bytes) {
		Date date = processDateStr(dateStr);
		if(date == null) {
			return new ResponseEntity<String>("Invalid date format", HttpStatus.BAD_REQUEST);
		}

		DataHandler.DataError error = DataHandler.INSTANCE.logData(phoneNumber, date, Integer.valueOf(hour), Integer.valueOf(bytes));

		if(error != null) {
			return new ResponseEntity<String>(error.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>(HttpStatus.CREATED);
	}

    @RequestMapping(value = "/request_data", method = RequestMethod.GET)
    public @ResponseBody List<UsageHistory> requestData(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="beginDate") String beginDateStr, @RequestParam(value="endDate") String endDateStr,
    		HttpServletResponse response) {
		Date beginDate = processDateStr(beginDateStr);
		Date endDate = processDateStr(endDateStr);
		if(beginDate == null || endDate == null) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
        List<UsageHistory> usageHistory = DataHandler.INSTANCE.getUsageData(phoneNumber, beginDate, endDate);
        
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
		Date beginDate = processDateStr(beginDateStr);
		Date endDate = processDateStr(endDateStr);
		if(beginDate == null || endDate == null) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		if(UserHandler.INSTANCE.validateUserAndPassword(phoneNumber, password) == null) {
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		
        List<UsageHistory> usageHistory = DataHandler.INSTANCE.getUserUsageData(phoneNumber, beginDate, endDate);
        
        if(usageHistory == null) {
        	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return usageHistory;
    }

    @RequestMapping(value = "/request_device_settings", method = RequestMethod.GET)
    public Device requestDeviceSettings(@RequestParam(value="phoneNumber") String phoneNumber) {
        Device device = DeviceHandler.INSTANCE.getDeviceSettings(phoneNumber);

        return device;
    }

    
    @RequestMapping(value = "/request_user_settings", method = RequestMethod.GET)
    public User requestUserSettings(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="password") String password) {
    	User user = UserHandler.INSTANCE.getUserSettings(phoneNumber, password);

        return user;
    }

    @RequestMapping(value = "/update_device_setting", method = RequestMethod.POST)
    public ResponseEntity<String> updateDeviceSetting(@RequestParam(value="phoneNumber") String phoneNumber,
    		@RequestParam(value="setting") String settingStr, @RequestParam(value="value") String value) {
    	DeviceSettings setting = DeviceSettings.valueOf(settingStr);
    	if(setting == null) {
    		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    	}

        boolean success = DeviceHandler.INSTANCE.setDeviceSetting(phoneNumber, setting, value);

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

        boolean success = UserHandler.INSTANCE.setUserSetting(phoneNumber, password, setting, value);

        if(success) {
        	return new ResponseEntity<String>(HttpStatus.OK);
        }
        else {
        	return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

	@RequestMapping(value = "/validate_email", method = RequestMethod.POST)
	public ResponseEntity<String> validateEmail(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "code") String code) {
		boolean error = UserHandler.INSTANCE.validateEmail(phoneNumber, code);
		
		if(error) {
			return new ResponseEntity<String>("Invalid code", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>(HttpStatus.OK);
	}
    
	/*@RequestMapping("/login")
	public String userLogin(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password) {
		return RegistrationHandler.INSTANCE.registerUser(phoneNumber, password).toString();
	}*/
}