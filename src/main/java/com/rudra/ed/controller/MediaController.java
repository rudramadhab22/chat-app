package com.rudra.ed.controller;

import com.rudra.ed.config.openapi.OpenApiTags;
import com.rudra.ed.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.MEDIA, description = "Public media assets such as profile pictures")
public class MediaController {

	private final FileStorageService fileStorageService;

	@GetMapping("/avatars/{filename}")
	@SecurityRequirements
	@Operation(summary = "Get avatar image", description = "Public endpoint — no JWT required.")
	public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
		Resource resource = fileStorageService.loadAvatar(filename);
		String contentType = probeContentType(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
				.contentType(MediaType.parseMediaType(contentType))
				.body(resource);
	}

	private String probeContentType(String filename) {
		String lower = filename.toLowerCase();
		if (lower.endsWith(".png")) {
			return "image/png";
		}
		if (lower.endsWith(".webp")) {
			return "image/webp";
		}
		if (lower.endsWith(".gif")) {
			return "image/gif";
		}
		return "image/jpeg";
	}
}
