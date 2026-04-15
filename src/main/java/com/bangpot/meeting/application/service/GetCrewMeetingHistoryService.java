package com.bangpot.meeting.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.port.MeetingHistoryReadRepository;
import com.bangpot.meeting.application.usecase.GetCrewMeetingHistoryUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMeetingHistoryService implements GetCrewMeetingHistoryUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingHistoryReadRepository meetingHistoryReadRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "meeting history access requires a completed user");
		crewRepository.findById(query.crewId()).orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		if (!crewMemberRepository.existsByCrewIdAndUserId(query.crewId(), query.userId())) {
			throw new AccessDeniedException("meeting history access requires an active crew membership");
		}

		MeetingHistoryReadRepository.SearchResult searchResult = meetingHistoryReadRepository.search(
			query.crewId(),
			query.userId(),
			query.page(),
			query.size()
		);

		List<Item> items = searchResult.items().stream()
			.map(item -> Item.of(
				item.meetingId(),
				item.meetingTitle(),
				item.themeName(),
				item.place(),
				item.date(),
				item.result(),
				item.logId() != null ? "HAS_LOG" : "NO_LOG",
				item.logId(),
				item.reviewSummary(),
				item.logCount(),
				item.participantCount(),
				item.coverPhotoUrl()
			))
			.toList();

		return Result.of(items, PageInfo.of(
			searchResult.pageInfo().page(),
			searchResult.pageInfo().size(),
			searchResult.pageInfo().hasNext()
		));
	}
}
