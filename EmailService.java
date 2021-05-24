package com.smart.service;


import javax.mail.PasswordAuthentication;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService
{
	public boolean sendEmail(String subject,String message,String to)
	{
		
		
		boolean f=false;
		String from="sub721131@gmail.com";
		
//		variable for email
		
		String host="smtp.gmail.com";
		
//		get the system property
		
		Properties properties=System.getProperties();
		System.out.println("PROPERTIES"+properties);
		
//		setting important information to properties object
		
		//host set
		
		properties.put("mail.smtp.host",host);
		properties.put("mail.smtp.port","465");
		properties.put("mail.smtp.ssl.enable","true");
		properties.put("mail.smtp,auth","true");
		
		
//		Step 1:to get the session object
		
		Session session=Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {				
				return new PasswordAuthentication("sub721131@gmail.com", "badboy101@");
			}
			
			
			
		});
		
	
		
		session.setDebug(true);
		
		
//		Step 2:compose the message(text,multimedia)
		
		MimeMessage m=new MimeMessage(session);
		try
		{
//			from email
			m.setFrom(from);
			
//			adding recipient to message
			
			m.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
			
//			adding subject to message
			
			m.setSubject(subject);
			
//			adding text to text
			
			m.setText(message);
			
//			send
			
//			Step 3:send the message using Transport class
			
			Transport.send(m);
			System.out.println("Sent success.....");
			f=false;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return f;
		
		
	}
}
