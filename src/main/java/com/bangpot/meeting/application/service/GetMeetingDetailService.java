package com.bangpot.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.domain.view.MeetingDetailView;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMeetingDetailService implements GetMeetingDetailUseCase {

	private static final String ACCESS_DENIED_MESSAGE = "媛?낇븳 ?щ（?먮쭔 紐⑥엫 ?곸꽭瑜?議고쉶?????덉뒿?덈떎.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingQueryRepository meetingQueryRepository;

	@Override
	@Transactional(readOnly = true)
	public MeetingDetailView handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), ACCESS_DENIED_MESSAGE);

		MeetingsAccessView access = meetingQueryRepository.findMeetingsAccessViewByCrewIdAndUserId(
			query.crewId(),
			query.userId()
		).orElseThrow(() -> new CrewNotFoundException(query.crewId()));
		if (access.myRole() == null) {
			throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
		}

		return meetingQueryRepository.findMeetingDetailView(
			query.crewId(),
			query.meetingId(),
			query.userId()
		).orElseThrow(() -> new MeetingNotFoundException(query.meetingId()));
	}
}
