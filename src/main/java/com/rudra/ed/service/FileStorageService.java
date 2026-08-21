package com.rudra.ed.service;

import com.rudra.ed.config.UploadProperties;
import com.rudra.ed.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/jpeg",
			"image/png",
			"image/webp",
			"image/gif"
	);

	private final UploadProperties uploadProperties;
	private Path avatarRoot;

	@PostConstruct
	void init() throws IOException {
		avatarRoot = Paths.get(uploadProperties.dir(), "avatars").toAbsolutePath().normalize();
		Files.createDirectories(avatarRoot);
	}

	public String storeAvatar(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("Avatar file is required");
		}
		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new BadRequestException("Only JPEG, PNG, WEBP, or GIF images are allowed");
		}

		String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename());
		String extension = "";
		int dot = original.lastIndexOf('.');
		if (dot >= 0) {
			extension = original.substring(dot);
		} else {
			extension = switch (contentType) {
				case "image/png" -> ".png";
				case "image/webp" -> ".webp";
				case "image/gif" -> ".gif";
				default -> ".jpg";
			};
		}

		String filename = UUID.randomUUID() + extension;
		try {
			Path target = avatarRoot.resolve(filename);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
			return "/api/v1/media/avatars/" + filename;
		} catch (IOException ex) {
			throw new BadRequestException("Failed to store avatar file");
		}
	}

	public Resource loadAvatar(String filename) {
		try {
			Path file = avatarRoot.resolve(filename).normalize();
			if (!file.startsWith(avatarRoot)) {
				throw new BadRequestException("Invalid file path");
			}
			Resource resource = new UrlResource(file.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new BadRequestException("File not found");
			}
			return resource;
		} catch (MalformedURLException ex) {
			throw new BadRequestException("File not found");
		}
	}
}
