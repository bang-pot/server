package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.MyMeetingLogReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyMeetingLogsUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyMeetingLogsService implements GetMyMeetingLogsUseCase {

	private static final int EXCERPT_LIMIT = 120;

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final MyMeetingLogReadRepository myMeetingLogReadRepository;

	@Override
	public Result handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));

		MyMeetingLogReadRepository.SearchResult searchResult = myMeetingLogReadRepository.search(
			user.getId(),
			query.page(),
			query.size()
		);

		return Result.of(
			searchResult.items().stream()
				.map(item -> Item.of(
					item.logId(),
					item.crewId(),
					item.crewName(),
					item.meetingId(),
					item.meetingTitle(),
					item.meetingDate(),
					item.createdAt(),
					toExcerpt(item.body()),
					item.coverPhotoUrl(),
					item.photoCount() == null ? 0L : item.photoCount()
				))
				.toList(),
			PageInfo.of(
				searchResult.pageInfo().page(),
				searchResult.pageInfo().size(),
				searchResult.pageInfo().hasNext()
			)
		);
	}

	private String toExcerpt(String body) {
		if (body == null || body.length() <= EXCERPT_LIMIT) {
			return body;
		}
		return body.substring(0, EXCERPT_LIMIT);
	}
}
