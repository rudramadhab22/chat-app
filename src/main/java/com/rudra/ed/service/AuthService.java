package com.rudra.ed.service;

import com.rudra.ed.domain.entity.User;
import com.rudra.ed.dto.mapper.UserMapper;
import com.rudra.ed.dto.request.LoginRequest;
import com.rudra.ed.dto.request.RegisterRequest;
import com.rudra.ed.dto.response.AuthResponse;
import com.rudra.ed.dto.response.UserResponse;
import com.rudra.ed.exception.ConflictException;
import com.rudra.ed.exception.ResourceNotFoundException;
import com.rudra.ed.repository.UserRepository;
import com.rudra.ed.security.JwtService;
import com.rudra.ed.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new ConflictException("Username is already taken");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ConflictException("Email is already registered");
		}

		User user = User.builder()
				.username(request.getUsername().trim())
				.email(request.getEmail().trim().toLowerCase())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.displayName(request.getDisplayName().trim())
				.about("Hey there! I am using ChatApp.")
				.build();

		User saved = userRepository.save(user);
		UserPrincipal principal = new UserPrincipal(saved);
		String token = jwtService.generateToken(principal);

		return AuthResponse.builder()
				.accessToken(token)
				.tokenType("Bearer")
				.user(UserMapper.toResponse(saved))
				.build();
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
		);
		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		User user = userRepository.findById(principal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		String token = jwtService.generateToken(principal);

		return AuthResponse.builder()
				.accessToken(token)
				.tokenType("Bearer")
				.user(UserMapper.toResponse(user))
				.build();
	}

	@Transactional(readOnly = true)
	public UserResponse me(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return UserMapper.toResponse(user);
	}
}
