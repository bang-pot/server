package com.bangpot.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bangpot.meeting.application.exception.MeetingLogNotFoundException;
import com.bangpot.meeting.application.port.MeetingLogPhotoRepository;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteMeetingLogService implements DeleteMeetingLogUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingLogRepository meetingLogRepository;
	private final MeetingLogPhotoRepository meetingLogPhotoRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "meeting log delete requires a completed user");
		MeetingLog log = meetingLogRepository.findById(command.logId())
			.orElseThrow(() -> new MeetingLogNotFoundException(command.logId()));
		if (!log.getAuthorUserId().equals(command.userId())) {
			throw new AccessDeniedException("작성자 본인만 meeting log를 삭제할 수 있습니다.");
		}
		meetingLogPhotoRepository.deleteByLogId(log.getId());
		meetingLogRepository.delete(log);
		return Result.of(log.getId());
	}
}

