package datatrackerserver.email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class EmailManager {

	public static final EmailManager INSTANCE = new EmailManager();

	private final String hostAddress;
	

	protected EmailManager() {
		hostAddress = "dannymurray05@gmail.com";
	}

	
	public boolean sendEmailConfirmationRequest(final String recipient, final String userPhoneNumber,
			final String confirmationCode) {
		String confirmationURL = "http://192.168.1.2:8080/validate_email?phoneNumber=" + userPhoneNumber
				+ "&code=" + confirmationCode;
		String subject = "DataTracker - confirm email request";
		String message = "To confirm your email please click the link below:\n\n"
				+ confirmationURL
				+ "\n\nThank you - DataTracker";
		return sendEmail(recipient, subject, message);
	}

	public boolean sendDeviceConfirmationRequest(final String recipient, final String userPhoneNumber,
			final String devicePhoneNumber, final String confirmationCode) {
		String confirmationURL = "http://192.168.1.2:8080/validate_device?phoneNumber=" + userPhoneNumber
				+ "&devicePhoneNumber=" + devicePhoneNumber
				+ "&code=" + confirmationCode;
		String subject = "DataTracker - confirm device request (" + devicePhoneNumber + ")";
		String message = "The device with phone number " + devicePhoneNumber + " has requested "
				+ "to be part of your DataTracker account. To confirm please click the link below:\n\n"
				+ confirmationURL
				+ "\n\nThank you - DataTracker";
		return sendEmail(recipient, subject, message);
	}

	public boolean sendEmail(final String recipient, final String subjectStr, final String messageStr) {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(hostAddress);
		
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			helper.setTo(recipient);
			helper.setText(messageStr);
			helper.setSubject(subjectStr);
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			sender.send(message);
		} catch (MailException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public String getHostAddress() {
		return hostAddress;
	}
}
