package com.rudra.ed.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration for the chat API.
 * <p>
 * Documents REST endpoints only. Realtime WebSocket contracts remain in README
 * because OpenAPI does not model raw WebSocket frames.
 */
@Configuration
public class OpenApiConfig {

	@Value("${server.port:8080}")
	private int serverPort;

	@Bean
	public OpenAPI chatAppOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Chat App API")
						.description("""
								Enterprise WhatsApp-style chat backend.

								**Auth:** obtain a JWT via `/api/v1/auth/login` or `/register`, then click \
								**Authorize** and paste `Bearer <token>` (or just the token).

								**Realtime:** connect to `ws://host:%d/ws/chat?token=<JWT>` — see README for \
								JSON envelope types (CHAT_MESSAGE, DELIVERED, READ, PRESENCE, etc.).
								""".formatted(serverPort))
						.version("v1")
						.contact(new Contact()
								.name("Chat App Backend")
								.email("support@chatapp.local"))
						.license(new License().name("Proprietary").url("https://localhost")))
				.servers(List.of(
						new Server().url("http://localhost:" + serverPort).description("Local development")
				))
				.externalDocs(new ExternalDocumentation()
						.description("Project README — REST + WebSocket protocol")
						.url("https://github.com/local/chat_app#readme"))
				.tags(List.of(
						new Tag().name(OpenApiTags.AUTH).description("Register, login, and current user profile"),
						new Tag().name(OpenApiTags.USERS).description("User search, profile updates, and avatars"),
						new Tag().name(OpenApiTags.CONVERSATIONS).description("Direct chats, groups, and membership"),
						new Tag().name(OpenApiTags.MESSAGES).description("Chat history and REST message send"),
						new Tag().name(OpenApiTags.MEDIA).description("Public media assets such as profile pictures")
				))
				.components(new Components()
						.addSecuritySchemes(OpenApiTags.SECURITY_SCHEME_BEARER, new SecurityScheme()
								.name(OpenApiTags.SECURITY_SCHEME_BEARER)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT access token from login/register")))
				.addSecurityItem(new SecurityRequirement().addList(OpenApiTags.SECURITY_SCHEME_BEARER));
	}

	@Bean
	public GroupedOpenApi authApi() {
		return GroupedOpenApi.builder()
				.group("1-authentication")
				.displayName("1. Authentication")
				.pathsToMatch("/api/v1/auth/**")
				.build();
	}

	@Bean
	public GroupedOpenApi usersApi() {
		return GroupedOpenApi.builder()
				.group("2-users-media")
				.displayName("2. Users & Media")
				.pathsToMatch("/api/v1/users/**", "/api/v1/media/**")
				.build();
	}

	@Bean
	public GroupedOpenApi chatApi() {
		return GroupedOpenApi.builder()
				.group("3-chat")
				.displayName("3. Conversations & Messages")
				.pathsToMatch("/api/v1/conversations/**")
				.build();
	}

	@Bean
	public GroupedOpenApi fullApi() {
		return GroupedOpenApi.builder()
				.group("0-all")
				.displayName("0. All APIs")
				.pathsToMatch("/api/v1/**")
				.build();
	}
}
