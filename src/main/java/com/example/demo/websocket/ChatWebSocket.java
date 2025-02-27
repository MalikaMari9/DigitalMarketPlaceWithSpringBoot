package com.example.demo.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chat")
@Component
public class ChatWebSocket {
	private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("WebSocket Opened: " + session.getId());
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		try {
			Map<String, Object> messageData = objectMapper.readValue(message, Map.class);
			String senderID = String.valueOf(messageData.get("senderID"));
			String receiverID = String.valueOf(messageData.get("receiverID"));
			String chatMessage = messageData.get("message") != null ? String.valueOf(messageData.get("message")) : "";

			userSessions.put(senderID, session);

			// Create a WebSocket message object
			Map<String, Object> responseMessage = Map.of("senderID", senderID, "receiverID", receiverID, "message",
					chatMessage);

			String jsonMessage = objectMapper.writeValueAsString(responseMessage);

			// Send to receiver
			Session receiverSession = userSessions.get(receiverID);
			if (receiverSession != null && receiverSession.isOpen()) {
				receiverSession.getBasicRemote().sendText(chatMessage);
			}

			// Send back to sender as well
			session.getBasicRemote().sendText(chatMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session) {
		userSessions.values().removeIf(s -> s.equals(session));
		System.out.println("WebSocket Closed: " + session.getId());
	}
}
