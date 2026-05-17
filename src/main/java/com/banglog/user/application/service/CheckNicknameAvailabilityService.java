package com.banglog.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.user.application.port.UserRepository;
import com.banglog.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.banglog.user.domain.service.NicknamePolicy;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CheckNicknameAvailabilityService implements CheckNicknameAvailabilityUseCase {

	private final UserRepository userRepository;

	@Override
	public Result handle(Query query) {
		String normalizedNickname = NicknamePolicy.normalize(query.nickname());
		if (normalizedNickname == null) {
			return Result.invalid();
		}
		return Result.of(normalizedNickname, !userRepository.existsByNickname(normalizedNickname));
	}
}
