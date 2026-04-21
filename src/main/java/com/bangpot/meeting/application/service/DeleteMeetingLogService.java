package com.bangpot.meeting.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.exception.MeetingLogNotFoundException;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.MeetingLogDeletedBy;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteMeetingLogService implements DeleteMeetingLogUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingLogRepository meetingLogRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "방탈로그 삭제는 가입 완료 사용자만 가능합니다.");
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException("meeting log delete requires an active crew membership");
		}

		MeetingLog log = meetingLogRepository.findById(command.logId())
			.orElseThrow(() -> new MeetingLogNotFoundException(command.logId()));
		meetingRepository.findByIdAndCrewId(log.getMeetingId(), command.crewId())
			.orElseThrow(() -> new MeetingLogNotFoundException(command.logId()));

		if (log.getAuthorUserId().equals(command.userId())) {
			log.delete(command.userId(), MeetingLogDeletedBy.AUTHOR, null, Instant.now());
			meetingLogRepository.save(log);
			return Result.of(log.getId(), DeletedBy.AUTHOR);
		}

		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(command.crewId(), command.userId())) {
			throw new AccessDeniedException("meeting log delete requires author or active crew leader");
		}

		String deleteReason = command.deleteReason() == null ? null : command.deleteReason().trim();
		if (deleteReason == null || deleteReason.isBlank()) {
			throw new MeetingLogRequestValidationException(
				List.of(new ApiErrorField("deleteReason", "크루장 운영 삭제에는 삭제 사유가 필요합니다."))
			);
		}

		log.delete(command.userId(), MeetingLogDeletedBy.LEADER, deleteReason, Instant.now());
		meetingLogRepository.save(log);
		return Result.of(log.getId(), DeletedBy.LEADER);
	}
}
