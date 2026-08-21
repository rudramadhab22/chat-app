package com.rudra.ed.util;

import com.rudra.ed.exception.ForbiddenException;
import com.rudra.ed.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static UserPrincipal currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			throw new ForbiddenException("Authentication required");
		}
		return principal;
	}

	public static Long currentUserId() {
		return currentUser().getId();
	}
}
