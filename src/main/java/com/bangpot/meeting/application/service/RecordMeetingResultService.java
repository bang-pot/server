package com.bangpot.meeting.application.service;

import java.time.Clock;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.exception.MeetingResultAlreadyRecordedException;
import com.bangpot.meeting.application.exception.MeetingResultRecordNotAllowedException;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.RecordMeetingResultUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingResult;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecordMeetingResultService implements RecordMeetingResultUseCase {

	private static final String MEMBER_RESULT_RECORD_DENIED_MESSAGE = "가입한 크루원만 모임 결과를 입력할 수 있습니다.";
	private static final String HOST_RESULT_RECORD_DENIED_MESSAGE = "모임 개설자만 결과를 입력할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final Clock clock;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.userId(), MEMBER_RESULT_RECORD_DENIED_MESSAGE);
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException(MEMBER_RESULT_RECORD_DENIED_MESSAGE);
		}

		Meeting meeting = meetingRepository.findByIdAndCrewId(command.meetingId(), command.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		if (!meeting.getHostUserId().equals(command.userId())) {
			throw new AccessDeniedException(HOST_RESULT_RECORD_DENIED_MESSAGE);
		}

		MeetingResult result = MeetingResult.valueOf(command.result());
		validateResultRecordable(meeting);

		int updatedCount = meetingRepository.recordResultIfNotRecorded(
			meeting.getId(),
			meeting.getCrewId(),
			command.userId(),
			result,
			clock.instant()
		);
		if (updatedCount == 0) {
			throw new MeetingResultAlreadyRecordedException(meeting.getId());
		}
		return Result.of(meeting.getId(), result.name());
	}

	private void validateResultRecordable(Meeting meeting) {
		if (meeting.getStatus() != MeetingStatus.COMPLETED) {
			throw new MeetingResultRecordNotAllowedException(meeting.getId(), meeting.getStatus().name());
		}
		if (meeting.getResult() != MeetingResult.NOT_RECORDED) {
			throw new MeetingResultAlreadyRecordedException(meeting.getId(), meeting.getResult().name());
		}
	}
}
