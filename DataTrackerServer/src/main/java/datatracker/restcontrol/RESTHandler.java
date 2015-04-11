package datatracker.restcontrol;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datatracker.datamangement.DataHandler;
import datatracker.usermanagement.RegistrationHandler;


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
		RegistrationHandler.RegistrationError error = RegistrationHandler.INSTANCE.registerUser(phoneNumber, password, email);
		
		if(error != null) {
			return new ResponseEntity<String>(error.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>(HttpStatus.CREATED);
	}

	@RequestMapping("/log_data")
	public ResponseEntity<String> logData(@RequestParam(value="phoneNumber") String phoneNumber, @RequestParam(value="date") String dateStr,
			@RequestParam(value="hour") String hour, @RequestParam(value="bytes") String bytes) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
		Date date = null;
		try {
			date = formatter.parse(dateStr);
		} catch (ParseException e) {
			
			e.printStackTrace();
			return new ResponseEntity<String>("Invalid date format", HttpStatus.BAD_REQUEST);
		}

		DataHandler.DataError error = DataHandler.INSTANCE.logData(phoneNumber, date, Integer.valueOf(hour), Integer.valueOf(bytes));
		if(error != null) {
			return new ResponseEntity<String>(error.getErrorMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>(HttpStatus.CREATED);
	}
	
	/*@RequestMapping("/login")
	public String userLogin(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password) {
		return RegistrationHandler.INSTANCE.registerUser(phoneNumber, password).toString();
	}*/
}