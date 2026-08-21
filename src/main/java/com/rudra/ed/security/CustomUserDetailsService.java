package com.rudra.ed.security;

import com.rudra.ed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.map(UserPrincipal::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}

	public UserDetails loadUserById(Long id) {
		return userRepository.findById(id)
				.map(UserPrincipal::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
	}
}
