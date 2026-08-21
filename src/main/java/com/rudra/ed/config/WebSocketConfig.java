package com.rudra.ed.config;

import com.rudra.ed.websocket.ChatWebSocketHandler;
import com.rudra.ed.websocket.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final ChatWebSocketHandler chatWebSocketHandler;
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chatWebSocketHandler, "/ws/chat")
				.addInterceptors(jwtHandshakeInterceptor)
				.setAllowedOrigins("*");
	}
}
