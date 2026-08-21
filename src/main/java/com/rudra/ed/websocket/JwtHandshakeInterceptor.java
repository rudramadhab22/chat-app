package com.rudra.ed.websocket;

import com.rudra.ed.security.CustomUserDetailsService;
import com.rudra.ed.security.JwtService;
import com.rudra.ed.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	public static final String USER_PRINCIPAL_ATTR = "userPrincipal";

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes
	) {
		String token = resolveToken(request);
		if (!StringUtils.hasText(token) || !jwtService.isTokenValid(token)) {
			return false;
		}
		try {
			String username = jwtService.extractUsername(token);
			UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
			if (!jwtService.isTokenValid(token, principal)) {
				return false;
			}
			attributes.put(USER_PRINCIPAL_ATTR, principal);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception
	) {
		// no-op
	}

	private String resolveToken(ServerHttpRequest request) {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			String queryToken = servletRequest.getServletRequest().getParameter("token");
			if (StringUtils.hasText(queryToken)) {
				return queryToken;
			}
		}
		String auth = request.getHeaders().getFirst("Authorization");
		if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
			return auth.substring(7);
		}
		return null;
	}
}
