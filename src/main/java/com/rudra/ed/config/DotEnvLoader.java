package com.rudra.ed.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

/**
 * Loads a local {@code .env} file into JVM system properties before Spring Boot starts.
 * <p>
 * Existing OS environment variables always win (they are never overwritten).
 * Missing {@code .env} is ignored so production can rely on real env vars only.
 */
public final class DotEnvLoader {

	private DotEnvLoader() {
	}

	public static void load() {
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.filename(".env")
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();

		for (DotenvEntry entry : dotenv.entries()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (isBlank(System.getenv(key)) && isBlank(System.getProperty(key))) {
				System.setProperty(key, value);
			}
		}
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
