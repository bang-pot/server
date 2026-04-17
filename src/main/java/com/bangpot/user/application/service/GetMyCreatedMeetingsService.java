package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.CreatedMeetingReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyCreatedMeetingsService implements GetMyCreatedMeetingsUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final CreatedMeetingReadRepository createdMeetingReadRepository;

	@Override
	public Result handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));

		CreatedMeetingReadRepository.SearchResult searchResult = createdMeetingReadRepository.search(user.getId(), query.page(), query.size());
		return Result.of(
			searchResult.items().stream()
				.map(item -> Item.of(
					item.meetingId(),
					item.title(),
					item.status(),
					item.date(),
					item.time(),
					item.crewId(),
					item.crewName()
				))
				.toList(),
			PageInfo.of(searchResult.pageInfo().page(), searchResult.pageInfo().size(), searchResult.pageInfo().hasNext())
		);
	}
}
