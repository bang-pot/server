package com.bangpot.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.exception.DuplicateNicknameException;
import com.bangpot.auth.application.exception.InvalidNicknameException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.auth.domain.AuthUser;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

	private final AuthUserRepository authUserRepository;

	@Override
	public Result handle(Command command) {
		AuthUser user = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (user.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}

		String normalizedNickname = normalizeNickname(command.nickname());
		if (normalizedNickname == null) {
			throw new InvalidNicknameException();
		}
		if (!normalizedNickname.equals(user.getNickname()) && authUserRepository.existsByNickname(normalizedNickname)) {
			throw new DuplicateNicknameException(normalizedNickname);
		}

		user.updateNickname(normalizedNickname);
		authUserRepository.save(user);
		return Result.of(user.getId(), user.getNickname());
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
