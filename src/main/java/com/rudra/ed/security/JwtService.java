package com.rudra.ed.security;

import com.rudra.ed.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(ensureMinKeyLength(jwtProperties.secret()));
	}

	private static byte[] ensureMinKeyLength(String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length >= 32) {
			return bytes;
		}
		byte[] padded = new byte[32];
		for (int i = 0; i < 32; i++) {
			padded[i] = bytes[i % bytes.length];
		}
		return padded;
	}

	public String generateToken(UserPrincipal principal) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.expirationMs());
		return Jwts.builder()
				.subject(principal.getUsername())
				.claims(Map.of(
						"uid", principal.getId(),
						"email", principal.getEmail(),
						"displayName", principal.getDisplayName()
				))
				.issuedAt(now)
				.expiration(expiry)
				.signWith(secretKey)
				.compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public Long extractUserId(String token) {
		Object uid = parseClaims(token).get("uid");
		if (uid instanceof Number number) {
			return number.longValue();
		}
		return Long.valueOf(uid.toString());
	}

	public boolean isTokenValid(String token, UserPrincipal principal) {
		String username = extractUsername(token);
		return username.equals(principal.getUsername()) && !isExpired(token);
	}

	public boolean isTokenValid(String token) {
		try {
			return !isExpired(token);
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean isExpired(String token) {
		return parseClaims(token).getExpiration().before(new Date());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
