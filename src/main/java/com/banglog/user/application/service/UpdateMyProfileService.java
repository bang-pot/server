package com.banglog.user.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.image.application.usecase.AttachImageUploadUseCase;
import com.banglog.image.domain.ImageUploadCategory;
import com.banglog.user.application.exception.DuplicateNicknameException;
import com.banglog.user.application.exception.InvalidNicknameException;
import com.banglog.user.application.port.UserRepository;
import com.banglog.user.application.usecase.UpdateMyProfileUseCase;
import com.banglog.user.domain.User;
import com.banglog.user.domain.service.NicknamePolicy;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

	private static final String COMPLETED_USER_REQUIRED_MESSAGE = "완료된 사용자만 프로필을 수정할 수 있습니다.";
	private static final String PROFILE_UPDATE_DENIED_MESSAGE = "프로필 수정 권한이 없습니다.";

	private final UserRepository userRepository;
	private final AttachImageUploadUseCase attachImageUploadUseCase;

	@Override
	public void handle(Command command) {
		User user = userRepository.findById(command.userId())
			.orElseThrow(() -> new AccessDeniedException(COMPLETED_USER_REQUIRED_MESSAGE));

		String normalizedNickname = NicknamePolicy.normalize(command.nickname());
		if (normalizedNickname == null) {
			throw new InvalidNicknameException();
		}
		if (!normalizedNickname.equals(user.getNickname()) && userRepository.existsByNickname(normalizedNickname)) {
			throw new DuplicateNicknameException(normalizedNickname);
		}

		if (!updateProfile(command, normalizedNickname)) {
			throw new AccessDeniedException(PROFILE_UPDATE_DENIED_MESSAGE);
		}
	}

	private boolean updateProfile(Command command, String normalizedNickname) {
		if (command.profileImageUploadId() == null) {
			return userRepository.updateNickname(command.userId(), normalizedNickname);
		}
		String profileImageUrl = attachImageUploadUseCase.handle(AttachImageUploadUseCase.Command.of(
			command.userId(),
			ImageUploadCategory.PROFILE_IMAGE,
			List.of(command.profileImageUploadId())
		)).urls().get(0);
		return userRepository.updateProfile(command.userId(), normalizedNickname, profileImageUrl);
	}
}
