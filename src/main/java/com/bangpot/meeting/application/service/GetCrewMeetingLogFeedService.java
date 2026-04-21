package com.bangpot.meeting.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.port.MeetingLogFeedReadRepository;
import com.bangpot.meeting.application.usecase.GetCrewMeetingLogFeedUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMeetingLogFeedService implements GetCrewMeetingLogFeedUseCase {

	private static final int EXCERPT_LIMIT = 120;

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingLogFeedReadRepository meetingLogFeedReadRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "방탈로그 피드 조회는 가입 완료 사용자만 가능합니다.");
		crewRepository.findById(query.crewId()).orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		if (!crewMemberRepository.existsByCrewIdAndUserId(query.crewId(), query.userId())) {
			throw new AccessDeniedException("meeting log feed access requires an active crew membership");
		}

		MeetingLogFeedReadRepository.SearchResult searchResult = meetingLogFeedReadRepository.search(
			query.crewId(),
			query.page(),
			query.size()
		);

		List<Item> items = searchResult.items().stream()
			.map(item -> Item.of(
				item.logId(),
				item.meetingId(),
				item.authorNickname(),
				item.meetingTitle(),
				item.meetingDate(),
				item.createdAt(),
				toExcerpt(item.body()),
				item.coverPhotoUrl(),
				toExtraPhotoCount(item.totalPhotoCount())
			))
			.toList();

		return Result.of(items, PageInfo.of(
			searchResult.pageInfo().page(),
			searchResult.pageInfo().size(),
			searchResult.pageInfo().hasNext()
		));
	}

	private String toExcerpt(String body) {
		if (body == null || body.length() <= EXCERPT_LIMIT) {
			return body;
		}
		return body.substring(0, EXCERPT_LIMIT);
	}

	private long toExtraPhotoCount(Long totalPhotoCount) {
		if (totalPhotoCount == null || totalPhotoCount <= 1L) {
			return 0L;
		}
		return totalPhotoCount - 1L;
	}
}
