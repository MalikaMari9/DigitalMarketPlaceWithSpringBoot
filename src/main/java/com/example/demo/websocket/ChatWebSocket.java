package com.example.demo.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.demo.controller.chat.ChatWebSocketConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chat", configurator = ChatWebSocketConfigurator.class)

@Component
public class ChatWebSocket {

	private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@OnOpen
	public void onOpen(Session session) {
		// Get user_id from query parameters safely
		Map<String, java.util.List<String>> params = session.getRequestParameterMap();
		if (params.containsKey("user_id") && !params.get("user_id").isEmpty()) {
			String userId = params.get("user_id").get(0);
			userSessions.put(userId, session);
			System.out.println("WebSocket Opened: " + session.getId() + " for user: " + userId);
		} else {
			System.out.println("Missing user_id, closing session: " + session.getId());
			try {
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		try {
			// Parse JSON message
			Map<String, Object> messageData = objectMapper.readValue(message, Map.class);
			String senderID = String.valueOf(messageData.get("senderID"));
			String receiverID = String.valueOf(messageData.get("receiverID"));
			String chatMessage = messageData.get("message") != null ? String.valueOf(messageData.get("message")) : "";

			// Prepare JSON response
			Map<String, Object> responseMessage = Map.of("senderID", senderID, "receiverID", receiverID, "message",
					chatMessage);

			String jsonMessage = objectMapper.writeValueAsString(responseMessage);

			// Send to receiver
			Session receiverSession = userSessions.get(receiverID);
			if (receiverSession != null && receiverSession.isOpen()) {
				System.out.println("Sending to receiver: " + receiverID);
				receiverSession.getBasicRemote().sendText(jsonMessage); // ✅ Send full JSON message
			} else {
				System.out.println("Receiver is offline: " + receiverID);
			}

			// Send back to sender as well
			session.getBasicRemote().sendText(jsonMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session) {
		// Remove user session safely
		userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
		System.out.println("WebSocket Closed: " + session.getId());
	}
}
