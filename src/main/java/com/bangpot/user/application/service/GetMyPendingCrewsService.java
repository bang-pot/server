package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.PendingCrewReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyPendingCrewsUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyPendingCrewsService implements GetMyPendingCrewsUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final PendingCrewReadRepository pendingCrewReadRepository;

	@Override
	public Result handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.isTemp()) {
			throw new AccessDeniedException("가입 완료 사용자만 이용할 수 있습니다.");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));

		PendingCrewReadRepository.SearchResult searchResult = pendingCrewReadRepository.search(
			user.getId(),
			query.page(),
			query.size()
		);
		return Result.of(
			searchResult.items().stream()
				.map(item -> Item.of(
					item.joinRequestId(),
					item.crewId(),
					item.crewName(),
					item.requestedAt(),
					item.messageSummary()
				))
				.toList(),
			PageInfo.of(searchResult.pageInfo().page(), searchResult.pageInfo().size(), searchResult.pageInfo().hasNext())
		);
	}
}
