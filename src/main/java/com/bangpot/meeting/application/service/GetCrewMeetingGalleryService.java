package com.bangpot.meeting.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.port.MeetingGalleryReadRepository;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMeetingGalleryService implements GetCrewMeetingGalleryUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingGalleryReadRepository meetingGalleryReadRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "meeting gallery access requires a completed user");
		crewRepository.findById(query.crewId()).orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		if (!crewMemberRepository.existsByCrewIdAndUserId(query.crewId(), query.userId())) {
			throw new AccessDeniedException("meeting gallery access requires an active crew membership");
		}

		MeetingGalleryReadRepository.SearchResult searchResult = meetingGalleryReadRepository.search(
			query.crewId(),
			query.page(),
			query.size()
		);

		List<Item> items = searchResult.items().stream()
			.map(item -> Item.of(
				item.meetingId(),
				item.meetingDate(),
				item.meetingTitle(),
				item.coverPhotoUrl(),
				item.extraPhotoCount()
			))
			.toList();

		return Result.of(items, PageInfo.of(
			searchResult.pageInfo().page(),
			searchResult.pageInfo().size(),
			searchResult.pageInfo().hasNext()
		));
	}
}
