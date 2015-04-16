package datatracker.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatracker.entities.User;
import datatracker.repositories.UserRepository;

@Configuration
@EnableAutoConfiguration
public class UserHandler {
	public static final UserHandler INSTANCE = new UserHandler();
	
	@Autowired
	private ApplicationContext appContext;
	
	public static enum RegistrationError {
		INVALID_NUMBER("Invalid phone number: Phone number must be 9 digits (including area code)."),
		INVALID_PASSWORD("Invalid password: The password must be at least 8 characters long and include at least one non-alphanumeric character."),
		INVALID_EMAIL("Invalid email: Email address must be a valid email address."),
		USER_ALREADY_EXISTS("User already exists: The phone number given is already in use."),
		EMAIL_ALREADY_EXISTS("Email already exists: The email given is already in use."),
		;

		public final String errorMessage;

		RegistrationError(String message) {
			errorMessage = message;
		}
		
		public String getErrorMessage() {
			return errorMessage;
		}
	}

	protected UserHandler() {}
	
	public RegistrationError registerUser(String phoneNumber, String password, String email) {
		UserRepository userRepo = appContext.getBean(UserRepository.class);
		User newUser = userRepo.findOne(phoneNumber);
		User userByEmail = userRepo.findByEmail(email);
		if(newUser != null) {
			System.out.println("User already exists");
			return RegistrationError.USER_ALREADY_EXISTS;
		}
		else if(userByEmail != null) {
			System.out.println("Email already exists");
			return RegistrationError.EMAIL_ALREADY_EXISTS;
		}
		else {
			newUser = new User(phoneNumber, password, email, 0, 0);
			userRepo.save(newUser);
		}
		
		
		Iterable<User> users = userRepo.findAll();
		for(User user : users) {
			System.out.println(user.toString());
		}
		return null; //success
	}

	public boolean validateUserAndPassword(String phoneNumber, String password) {
		UserRepository userRepo = appContext.getBean(UserRepository.class);
		User newUser = userRepo.findOne(phoneNumber);
		
		if(newUser == null) {
			return false;
		}

		if(!password.equals(newUser.getPassword())) {
			return false;
		}

		return true;
	}
	
	public boolean validateEmail(String phoneNumber, String code) {
		UserRepository userRepo = appContext.getBean(UserRepository.class);
		User newUser = userRepo.findOne(phoneNumber);
		
		if(newUser.getValidationCode().equals(code)) {
			newUser.setEmailValidated(true);
			userRepo.save(newUser);
			return true;
		}

		return false;
	}

	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}
}
