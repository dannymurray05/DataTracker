package datatrackerserver.entitymanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatrackerserver.entities.User;
import datatrackerserver.repositories.UserRepository;

@Configuration
@EnableAutoConfiguration
public class UserHandler {
	public static final UserHandler INSTANCE = new UserHandler();
	
	@Autowired
	private ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}
	
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
	
	public static enum UserSettings {
		BILLING_CYCLE_LENGTH,
		QUOTA,
		THRESHOLD,
		;
	}

	protected UserHandler() {}

	//TODO !!!! add all registration/validation mappings and handler functions
	
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
			//create the corresponding device for this user
			DeviceHandler.INSTANCE.registerDevice(phoneNumber, newUser);
		}
		
		//debug
		Iterable<User> users = userRepo.findAll();
		for(User user : users) {
			System.out.println(user.toString());
		}
		//end debug

		return null; //success
	}

	public User validateUserAndPassword(String phoneNumber, String password) {
		UserRepository userRepo = appContext.getBean(UserRepository.class);
		User user = userRepo.findOne(phoneNumber);
		
		if(user == null) {
			return null;
		}

		if(!password.equals(user.getPassword())) {
			return null;
		}

		return user;
	}

	public User getUserSettings(String phoneNumber, String password) {
		return validateUserAndPassword(phoneNumber, password);
	}

	public boolean setUserSetting(String phoneNumber, String password,
			UserSettings setting, String value) {
		boolean success = false;
		UserRepository userRepo = appContext.getBean(UserRepository.class);
		User user = validateUserAndPassword(phoneNumber, password);
		
		if(user == null) {
			return false;
		}

		switch(setting) {
			case QUOTA:
				long quota = 0;

				try {
					quota = Long.valueOf(value);
				}
				catch(NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("Invalid quota string: " + value);
					break;
				}
				
				user.setQuota(quota);
				success = true;
				break;
			case THRESHOLD:
				long threshold = 0;
				try {
					threshold = Long.valueOf(value);
				}
				catch(NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("Invalid threshold string: " + value);
					break;
				}
				
				user.setThreshold(threshold);
				success = true;
				break;
		case BILLING_CYCLE_LENGTH:
			int cycleLength = 0;
			try {
				cycleLength = Integer.valueOf(value);
			}
			catch(NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("Invalid billing cycle string: " + value);
				break;
			}
			
			user.setBillingCycleLength(cycleLength);
			success = true;
			break;
		default:
			break;
		}
		
		if(success) {
			userRepo.save(user);
		}

		return success;
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
}
