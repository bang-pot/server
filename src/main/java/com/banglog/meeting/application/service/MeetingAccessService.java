package com.banglog.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.meeting.application.port.MeetingQueryRepository;
import com.banglog.meeting.domain.view.MeetingsAccessView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingAccessService {

	private final MeetingQueryRepository meetingQueryRepository;

	public MeetingsAccessView validateActiveCrewMember(Long crewId, Long userId, String deniedMessage) {
		MeetingsAccessView access = meetingQueryRepository.findMeetingsAccessViewByCrewIdAndUserId(crewId, userId)
			.orElseThrow(() -> new CrewNotFoundException(crewId));
		if (access.myRole() == null) {
			throw new AccessDeniedException(deniedMessage);
		}
		return access;
	}
}
