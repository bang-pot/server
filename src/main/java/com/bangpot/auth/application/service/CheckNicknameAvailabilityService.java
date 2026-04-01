package com.bangpot.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.usecase.CheckNicknameAvailabilityUseCase;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CheckNicknameAvailabilityService implements CheckNicknameAvailabilityUseCase {

	private final AuthUserRepository authUserRepository;

	@Override
	public Result handle(Query query) {
		String normalizedNickname = normalizeNickname(query.nickname());
		if (normalizedNickname == null) {
			return Result.invalid();
		}
		return Result.of(
			normalizedNickname,
			!authUserRepository.existsByNickname(normalizedNickname)
		);
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
