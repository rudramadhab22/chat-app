package com.rudra.ed.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

	private final Map<Long, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();
	private final Map<String, Long> userBySessionId = new ConcurrentHashMap<>();

	public void register(Long userId, WebSocketSession session) {
		sessionsByUser.computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet()).add(session);
		userBySessionId.put(session.getId(), userId);
	}

	public void remove(WebSocketSession session) {
		Long userId = userBySessionId.remove(session.getId());
		if (userId == null) {
			return;
		}
		Set<WebSocketSession> sessions = sessionsByUser.get(userId);
		if (sessions != null) {
			sessions.remove(session);
			if (sessions.isEmpty()) {
				sessionsByUser.remove(userId);
			}
		}
	}

	public boolean hasActiveSessions(Long userId) {
		Set<WebSocketSession> sessions = sessionsByUser.get(userId);
		return sessions != null && !sessions.isEmpty();
	}

	public Set<WebSocketSession> getSessions(Long userId) {
		return sessionsByUser.getOrDefault(userId, Collections.emptySet());
	}

	public Long getUserId(WebSocketSession session) {
		return userBySessionId.get(session.getId());
	}

	public void sendToUser(Long userId, String payload) throws IOException {
		for (WebSocketSession session : getSessions(userId)) {
			if (session.isOpen()) {
				synchronized (session) {
					session.sendMessage(new org.springframework.web.socket.TextMessage(payload));
				}
			}
		}
	}
}
