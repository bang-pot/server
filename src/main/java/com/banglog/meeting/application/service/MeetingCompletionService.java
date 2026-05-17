package com.banglog.meeting.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingCompletionService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

	private final MeetingRepository meetingRepository;
	private final Clock clock;

	public void completeTargets(int limit) {
		for (Meeting meeting : meetingRepository.findCompletionTargets(now().minusHours(6), limit)) {
			completeAndSaveIfNeeded(meeting);
		}
	}

	public void completeAndSaveIfNeeded(Meeting meeting) {
		MeetingStatus before = meeting.getStatus();
		meeting.completeAutomatically(now());
		if (before != meeting.getStatus()) {
			meetingRepository.save(meeting);
		}
	}

	private LocalDateTime now() {
		return clock.instant().atZone(BUSINESS_ZONE).toLocalDateTime();
	}
}
