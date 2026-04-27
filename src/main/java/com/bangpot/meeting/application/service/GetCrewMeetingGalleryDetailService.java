package com.bangpot.meeting.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.meeting.application.exception.MeetingGalleryNotFoundException;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryDetailUseCase;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMeetingGalleryDetailService implements GetCrewMeetingGalleryDetailUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "활성 크루 멤버만 모임 사진첩 상세를 조회할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public CrewMeetingGalleryDetailView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);

		MeetingsAccessView access = meetingQueryRepository.findMeetingsAccessViewByCrewIdAndUserId(
			query.crewId(),
			query.userId()
		).orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		if (access.myRole() == null) {
			throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
		}

		CrewMeetingGalleryDetailTargetView target = meetingQueryRepository.findCrewMeetingGalleryDetailTargetView(
			query.crewId(),
			query.meetingId()
		)
			.orElseThrow(() -> new MeetingGalleryNotFoundException(query.meetingId()));
		List<CrewMeetingGalleryDetailView.Photo> photos = meetingQueryRepository.findCrewMeetingGalleryDetailPhotos(
			target.meetingId()
		);

		return CrewMeetingGalleryDetailView.of(
			target.meetingId(),
			target.meetingDate(),
			target.meetingTitle(),
			photos,
			photos.size()
		);
	}
}
