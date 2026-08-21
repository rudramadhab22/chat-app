package com.rudra.ed.config.openapi;

/**
 * Central OpenAPI tag names used across controllers and {@link OpenApiConfig}.
 */
public final class OpenApiTags {

	public static final String AUTH = "Authentication";
	public static final String USERS = "Users";
	public static final String CONVERSATIONS = "Conversations";
	public static final String MESSAGES = "Messages";
	public static final String MEDIA = "Media";

	public static final String SECURITY_SCHEME_BEARER = "bearerAuth";

	private OpenApiTags() {
	}
}
