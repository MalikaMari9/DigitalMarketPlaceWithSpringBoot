package com.example.demo.controller.chat;

import jakarta.websocket.server.ServerEndpointConfig;

public class ChatWebSocketConfigurator extends ServerEndpointConfig.Configurator {
	@Override
	public boolean checkOrigin(String originHeaderValue) {
		System.out.println("WebSocket Origin Check: " + originHeaderValue);
		return true; // ✅ Allow WebSocket connections from any origin
	}
}
