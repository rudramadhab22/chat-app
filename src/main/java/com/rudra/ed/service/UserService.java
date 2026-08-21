package com.rudra.ed.service;

import com.rudra.ed.domain.entity.User;
import com.rudra.ed.dto.mapper.UserMapper;
import com.rudra.ed.dto.request.UpdateProfileRequest;
import com.rudra.ed.dto.response.UserResponse;
import com.rudra.ed.exception.ResourceNotFoundException;
import com.rudra.ed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;

	@Transactional(readOnly = true)
	public UserResponse getById(Long id) {
		return UserMapper.toResponse(requireUser(id));
	}

	@Transactional(readOnly = true)
	public List<UserResponse> search(String query) {
		if (!StringUtils.hasText(query) || query.trim().length() < 2) {
			return List.of();
		}
		return userRepository.search(query.trim()).stream()
				.map(UserMapper::toResponse)
				.toList();
	}

	@Transactional
	public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
		User user = requireUser(userId);
		if (StringUtils.hasText(request.getDisplayName())) {
			user.setDisplayName(request.getDisplayName().trim());
		}
		if (request.getAbout() != null) {
			user.setAbout(request.getAbout().trim());
		}
		return UserMapper.toResponse(userRepository.save(user));
	}

	@Transactional
	public UserResponse updateAvatar(Long userId, MultipartFile file) {
		User user = requireUser(userId);
		String url = fileStorageService.storeAvatar(file);
		user.setProfilePictureUrl(url);
		return UserMapper.toResponse(userRepository.save(user));
	}

	@Transactional
	public void setOnline(Long userId, boolean online) {
		User user = requireUser(userId);
		user.setOnline(online);
		user.setLastSeenAt(Instant.now());
		userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public User requireUser(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
