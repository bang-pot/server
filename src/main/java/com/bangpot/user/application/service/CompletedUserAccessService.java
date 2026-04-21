package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bangpot.user.application.port.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompletedUserAccessService {

	private final UserRepository userRepository;

	public boolean isCompletedUser(Long userId) {
		return userRepository.findById(userId).isPresent();
	}

	public void validateCompletedUser(Long userId, String deniedMessage) {
		if (!isCompletedUser(userId)) {
			throw new AccessDeniedException(deniedMessage);
		}
	}
}
