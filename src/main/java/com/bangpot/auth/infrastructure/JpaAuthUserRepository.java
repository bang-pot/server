package com.bangpot.auth.infrastructure;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;

@Repository
@RequiredArgsConstructor
public class JpaAuthUserRepository implements AuthUserRepository {

	private final AuthUserJpaRepository authUserJpaRepository;

	@Override
	public Optional<AuthUser> findById(Long userId) {
		return authUserJpaRepository.findById(userId);
	}

	@Override
	public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
		return authUserJpaRepository.findByProviderAndProviderId(provider, providerId);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return authUserJpaRepository.existsByNickname(nickname);
	}

	@Override
	public List<AuthUser> findFullUsersByNicknameContaining(String nickname) {
		return authUserJpaRepository.findAllFullUsersByNicknameContaining(
			AuthUserStatus.FULL,
			normalizeKeyword(nickname)
		);
	}

	@Override
	public AuthUser save(AuthUser user) {
		return authUserJpaRepository.save(user);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}
		String trimmed = keyword.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
