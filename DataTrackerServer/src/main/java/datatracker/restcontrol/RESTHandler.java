package datatracker.restcontrol;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import datatracker.datamangement.DataHandler;
import datatracker.entities.UsageHistory;
import datatracker.usermanagement.UserHandler;


/*@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication*/
@Component
@RestController
public class RESTHandler {
	
	@RequestMapping("/new_user")
	public ResponseEntity<String> registerUser(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password, @RequestParam(value = "email") String email) {
		UserHandler.RegistrationError error = UserHandler.INSTANCE.registerUser(phoneNumber, password, email);
		
		if(error != null) {
			return new ResponseEntity<String>(error.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>(HttpStatus.CREATED);
	}

	@RequestMapping("/log_data")
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

    @RequestMapping("/request_data")
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
    
    @RequestMapping("/request_user_data")
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

		if(!UserHandler.INSTANCE.validateUserAndPassword(phoneNumber, password)) {
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
    
    
    
    public Date processDateStr(String dateStr) {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	Date date = null;
		try {
			date = formatter.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return date;
		}
		
		return date;
    }
    
	/*@RequestMapping("/login")
	public String userLogin(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password) {
		return RegistrationHandler.INSTANCE.registerUser(phoneNumber, password).toString();
	}*/
}