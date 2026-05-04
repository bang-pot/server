package com.bangpot.meeting.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryUseCase;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMeetingGalleryService implements GetCrewMeetingGalleryUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "활성 크루 멤버만 모임 사진첩을 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingAccessService meetingAccessService;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewMeetingGalleryView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);
		meetingAccessService.validateActiveCrewMember(query.crewId(), query.userId(), ACCESS_DENIED_MESSAGE);

		return meetingQueryRepository.findCrewMeetingGalleryView(query.crewId(), query.page(), query.size());
	}
}
