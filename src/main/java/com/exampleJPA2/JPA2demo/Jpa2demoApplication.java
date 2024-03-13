package com.exampleJPA2.JPA2demo;

import com.exampleJPA2.JPA2demo.services.JavaSmptGmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class Jpa2demoApplication {
	@Autowired
	private JavaSmptGmailSenderService senderService;

	public static void main(String[] args) {
		SpringApplication.run(Jpa2demoApplication.class, args);
	}

//	@EventListener(ApplicationReadyEvent.class)
//	public void sendMail(){
//		senderService.sendEmail("juanchuu.jfpc@gmail.com","This is subject","This is email body");
//	}

}
