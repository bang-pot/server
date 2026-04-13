package com.bangpot.meeting.application.service;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingAutomaticTransitionService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final Clock clock;

	public Meeting apply(Meeting meeting) {
		if (meeting.getStatus() == MeetingStatus.CANCELED || meeting.getStatus() == MeetingStatus.COMPLETED) {
			return meeting;
		}

		MeetingStatus before = meeting.getStatus();
		long joinedCount = meetingParticipantRepository.countByMeetingId(meeting.getId()) + 1L;
		meeting.applyAutomaticTransition(clock.instant().atZone(BUSINESS_ZONE).toLocalDateTime(), joinedCount);

		if (before != meeting.getStatus()) {
			meetingRepository.save(meeting);
		}
		return meeting;
	}
}
