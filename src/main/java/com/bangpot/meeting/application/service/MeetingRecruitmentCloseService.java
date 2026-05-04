package com.bangpot.meeting.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingRecruitmentCloseService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final Clock clock;

	public void closeTargets(int limit) {
		for (Meeting meeting : meetingRepository.findRecruitmentCloseTargets(now(), limit)) {
			closeAndSaveIfNeeded(meeting);
		}
	}

	public void closeAndSaveIfNeeded(Meeting meeting) {
		MeetingStatus before = meeting.getStatus();
		long joinedCount = meetingParticipantRepository.countByMeetingId(meeting.getId()) + 1L;
		meeting.closeRecruitmentAutomatically(now(), joinedCount);
		if (before != meeting.getStatus()) {
			meetingRepository.save(meeting);
		}
	}

	private LocalDateTime now() {
		return clock.instant().atZone(BUSINESS_ZONE).toLocalDateTime();
	}
}
