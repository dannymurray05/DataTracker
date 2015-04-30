package datatrackerserver.entitymanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatrackerserver.email.EmailManager;
import datatrackerserver.entities.Account;
import datatrackerserver.repositories.AccountRepository;
import datatrackerserver.security.SecurityManager;

@Configuration
@EnableAutoConfiguration
public class AccountHandler {
	public static final long DEFAULT_QUOTA = (long) Math.pow(2, 30); //1GB
	public static final long DEFAULT_THRESHOLD = (long) (DEFAULT_QUOTA * .90); //90% of quota
	
	@Autowired
	private ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}
	
	public static enum RegistrationError {
		INVALID_NUMBER("Invalid phone number: Phone number must be 9 digits (including area code)."),
		INVALID_PASSWORD("Invalid password: The password must be at least 8 characters long and include at least one non-alphanumeric character."),
		INVALID_EMAIL("Invalid email: Email address must be a valid email address."),
		ACCOUNT_ALREADY_EXISTS("Account already exists: The phone number given is already in use."),
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
	
	public static enum AccountSettings {
		BILLING_CYCLE_LENGTH,
		QUOTA,
		THRESHOLD,
		;
	}

	protected AccountHandler() {}

	//TODO !!!! add all registration/validation mappings and handler functions
	
	public RegistrationError registerAccount(String phoneNumber, String password, String email) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account newAccount = accountRepo.findOne(phoneNumber);
		Account accountByEmail = accountRepo.findByEmail(email);
		if(newAccount != null) {
			System.out.println("Account already exists");
			return RegistrationError.ACCOUNT_ALREADY_EXISTS;
		}
		else if(accountByEmail != null) {
			System.out.println("Email already exists");
			return RegistrationError.EMAIL_ALREADY_EXISTS;
		}
		else {
			newAccount = new Account(phoneNumber, password, email,
					DEFAULT_QUOTA, DEFAULT_THRESHOLD);
			newAccount.setValidationCode(SecurityManager.generateRandomCode());
			appContext.getBean(EmailManager.class).sendEmailConfirmationRequest(
					email, phoneNumber, newAccount.getValidationCode());
			
			accountRepo.save(newAccount);
			
			//create the corresponding device for this account
			appContext.getBean(DeviceHandler.class).registerDevice(phoneNumber, newAccount);
		}
		
		//debug
		Iterable<Account> accounts = accountRepo.findAll();
		for(Account account : accounts) {
			System.out.println(account.toString());
		}
		//end debug

		return null; //success
	}

	public Account validateAccountAndPassword(String phoneNumber, String password) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account account = accountRepo.findOne(phoneNumber);
		
		if(account == null) {
			return null;
		}

		if(!password.equals(account.getPassword())) {
			return null;
		}

		return account;
	}

	public Account getAccountSettings(String phoneNumber, String password) {
		return validateAccountAndPassword(phoneNumber, password);
	}

	public boolean setAccountSetting(String phoneNumber, String password,
			AccountSettings setting, String value) {
		boolean success = false;
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account account = validateAccountAndPassword(phoneNumber, password);
		
		if(account == null) {
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
				
				account.setQuota(quota);
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
				
				account.setThreshold(threshold);
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
			
			account.setBillingCycleLength(cycleLength);
			success = true;
			break;
		default:
			break;
		}
		
		if(success) {
			accountRepo.save(account);
		}

		return success;
	}
	
	public boolean validateEmail(String phoneNumber, String code) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account newAccount = accountRepo.findOne(phoneNumber);
		
		if(newAccount.getValidationCode().equals(code)) {
			newAccount.setEmailValidated(true);
			accountRepo.save(newAccount);
			return true;
		}

		return false;
	}
}
