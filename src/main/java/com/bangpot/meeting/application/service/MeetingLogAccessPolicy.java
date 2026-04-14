package com.bangpot.meeting.application.service;

import java.util.Set;

import org.springframework.security.access.AccessDeniedException;

import com.bangpot.meeting.application.exception.MeetingLogWriteNotAllowedException;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;

final class MeetingLogAccessPolicy {

	private static final Set<MeetingParticipationStatus> WRITABLE_HISTORY_STATUSES = Set.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.PENDING,
		MeetingParticipationStatus.APPROVED
	);

	private MeetingLogAccessPolicy() {
	}

	static void validateWritableMeeting(Meeting meeting) {
		if (meeting.getStatus() != MeetingStatus.COMPLETED) {
			throw new MeetingLogWriteNotAllowedException(meeting.getId());
		}
	}

	static void validateParticipantHistory(
		Meeting meeting,
		Long userId,
		MeetingParticipantRepository meetingParticipantRepository
	) {
		if (meeting.getHostUserId().equals(userId)) {
			return;
		}
		boolean hasHistory = meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), userId)
			.map(participant -> WRITABLE_HISTORY_STATUSES.contains(participant.getStatus()))
			.orElse(false);
		if (!hasHistory) {
			throw new AccessDeniedException("완료된 meeting의 참여 이력이 있는 사용자만 meeting log를 작성할 수 있습니다.");
		}
	}
}

