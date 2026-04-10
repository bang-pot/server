package com.bangpot.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CheckNicknameAvailabilityService implements CheckNicknameAvailabilityUseCase {

	private final UserRepository userRepository;

	@Override
	public Result handle(Query query) {
		String normalizedNickname = normalizeNickname(query.nickname());
		if (normalizedNickname == null) {
			return Result.invalid();
		}
		return Result.of(normalizedNickname, !userRepository.existsByNickname(normalizedNickname));
	}

	private String normalizeNickname(String nickname) {
		if (nickname == null) {
			return null;
		}
		String normalizedNickname = nickname.trim();
		if (normalizedNickname.isEmpty()) {
			return null;
		}
		return normalizedNickname;
	}
}
