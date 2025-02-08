package com.example.demo;

import org.springframework.context.annotation.Configuration;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@Configuration
public class SessionConfig implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		event.getSession().setMaxInactiveInterval(7200); // 7200 seconds = 2 hours
		System.out.println("✅ Session Created: " + event.getSession().getId() + " - Timeout: 2 hours");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		System.out.println("❌ Session Expired: " + event.getSession().getId());
	}
}
