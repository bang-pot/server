package com.banglog.meeting.application.service;

import java.time.Clock;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.common.error.ApiErrorField;
import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.meeting.application.exception.MeetingLogNotFoundException;
import com.banglog.meeting.application.exception.MeetingLogRequestValidationException;
import com.banglog.meeting.application.port.MeetingLogRepository;
import com.banglog.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.banglog.meeting.domain.MeetingLog;
import com.banglog.meeting.domain.MeetingLogDeletedBy;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteMeetingLogService implements DeleteMeetingLogUseCase {

	private static final String COMPLETED_USER_REQUIRED_MESSAGE = "완료된 사용자만 방탈로그를 삭제할 수 있습니다.";
	private static final String ACTIVE_MEMBER_REQUIRED_MESSAGE = "활성 크루 멤버만 방탈로그를 삭제할 수 있습니다.";
	private static final String AUTHOR_OR_LEADER_REQUIRED_MESSAGE = "작성자 또는 크루장만 방탈로그를 삭제할 수 있습니다.";
	private static final String LEADER_DELETE_REASON_REQUIRED_MESSAGE = "크루장이 다른 회원의 방탈로그를 삭제하려면 삭제 사유가 필요합니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingLogRepository meetingLogRepository;
	private final Clock clock;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), COMPLETED_USER_REQUIRED_MESSAGE);
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException(ACTIVE_MEMBER_REQUIRED_MESSAGE);
		}

		MeetingLog log = meetingLogRepository.findActiveLogInCrewForUpdate(command.crewId(), command.logId())
			.orElseThrow(() -> new MeetingLogNotFoundException(command.logId()));

		if (log.getAuthorUserId().equals(command.userId())) {
			log.delete(command.userId(), MeetingLogDeletedBy.AUTHOR, null, clock.instant());
			return Result.of(log.getId(), DeletedBy.AUTHOR);
		}

		if (!crewMemberRepository.existsLeaderByCrewIdAndUserId(command.crewId(), command.userId())) {
			throw new AccessDeniedException(AUTHOR_OR_LEADER_REQUIRED_MESSAGE);
		}

		String deleteReason = command.deleteReason() == null ? null : command.deleteReason().trim();
		if (deleteReason == null || deleteReason.isBlank()) {
			throw new MeetingLogRequestValidationException(
				List.of(new ApiErrorField("deleteReason", LEADER_DELETE_REASON_REQUIRED_MESSAGE))
			);
		}

		log.delete(command.userId(), MeetingLogDeletedBy.LEADER, deleteReason, clock.instant());
		return Result.of(log.getId(), DeletedBy.LEADER);
	}
}
