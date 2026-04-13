package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.DuplicateNicknameException;
import com.bangpot.user.application.exception.InvalidNicknameException;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;

	@Override
	public Result handle(Command command) {
		AuthUser authUser = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}

		User user = userRepository.findById(command.userId())
			.orElseThrow(() -> new UserNotFoundException(command.userId()));

		String normalizedNickname = normalizeNickname(command.nickname());
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
