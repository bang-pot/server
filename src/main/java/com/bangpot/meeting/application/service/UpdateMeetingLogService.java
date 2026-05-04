package com.bangpot.meeting.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.exception.MeetingLogNotFoundException;
import com.bangpot.meeting.application.port.MeetingLogPhotoRepository;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.usecase.UpdateMeetingLogUseCase;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.MeetingLogPhoto;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMeetingLogService implements UpdateMeetingLogUseCase {

	private static final String COMPLETED_USER_REQUIRED_MESSAGE = "완료된 사용자만 방탈로그를 수정할 수 있습니다.";
	private static final String AUTHOR_REQUIRED_MESSAGE = "작성자 본인만 방탈로그를 수정할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingLogRepository meetingLogRepository;
	private final MeetingLogPhotoRepository meetingLogPhotoRepository;
	private final Clock clock;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), COMPLETED_USER_REQUIRED_MESSAGE);
		MeetingLog log = meetingLogRepository.findByIdForUpdate(command.logId())
			.orElseThrow(() -> new MeetingLogNotFoundException(command.logId()));
		validateAuthor(log, command.userId());
		MeetingLogCommandValidator.validateForUpdate(command.body(), command.photos());

		log.edit(command.body(), clock.instant());
		meetingLogPhotoRepository.deleteByLogId(log.getId());
		meetingLogPhotoRepository.saveAll(toPhotos(log.getId(), command.photos(), log.getUpdatedAt()));
		return Result.of(log.getId(), log.getMeetingId());
	}

	private void validateAuthor(MeetingLog log, Long userId) {
		if (!log.getAuthorUserId().equals(userId)) {
			throw new AccessDeniedException(AUTHOR_REQUIRED_MESSAGE);
		}
	}

	private List<MeetingLogPhoto> toPhotos(Long logId, List<UpdateMeetingLogUseCase.PhotoInput> photos, Instant now) {
		if (photos == null || photos.isEmpty()) {
			return List.of();
		}
		return photos.stream()
			.map(photo -> MeetingLogPhoto.create(logId, photo.url(), now))
			.toList();
	}
}
