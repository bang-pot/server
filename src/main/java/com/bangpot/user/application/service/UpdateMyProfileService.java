package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.user.application.exception.DuplicateNicknameException;
import com.bangpot.user.application.exception.InvalidNicknameException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.domain.service.NicknamePolicy;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

	private final UserRepository userRepository;

	@Override
	public Result handle(Command command) {
		User user = userRepository.findById(command.userId())
			.orElseThrow(() -> new AccessDeniedException("프로필 완료가 필요합니다."));

		String normalizedNickname = NicknamePolicy.normalize(command.nickname());
		if (normalizedNickname == null) {
			throw new InvalidNicknameException();
		}
		if (!normalizedNickname.equals(user.getNickname()) && userRepository.existsByNickname(normalizedNickname)) {
			throw new DuplicateNicknameException(normalizedNickname);
		}

		user.updateNickname(normalizedNickname);
		userRepository.save(user);
		return Result.of(user.getId(), user.getNickname());
	}
}
