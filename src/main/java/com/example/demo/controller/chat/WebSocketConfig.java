package com.example.demo.controller.chat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.websocket.ChatWebSocket;

import jakarta.websocket.server.ServerEndpointConfig;

@Configuration
public class WebSocketConfig {
	@Bean
	public ServerEndpointConfig chatEndpointConfig() {
		return ServerEndpointConfig.Builder.create(ChatWebSocket.class, "/chat")
				.configurator(new ChatWebSocketConfigurator()).build();
	}
}
