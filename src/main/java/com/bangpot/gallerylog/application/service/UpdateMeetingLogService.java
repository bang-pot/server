package com.bangpot.gallerylog.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bangpot.gallerylog.application.exception.MeetingLogNotFoundException;
import com.bangpot.gallerylog.application.port.MeetingLogPhotoRepository;
import com.bangpot.gallerylog.application.port.MeetingLogRepository;
import com.bangpot.gallerylog.application.usecase.UpdateMeetingLogUseCase;
import com.bangpot.gallerylog.domain.MeetingLog;
import com.bangpot.gallerylog.domain.MeetingLogPhoto;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMeetingLogService implements UpdateMeetingLogUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingLogRepository meetingLogRepository;
	private final MeetingLogPhotoRepository meetingLogPhotoRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "방탈로그를 수정할 수 없습니다.");
		MeetingLog log = meetingLogRepository.findById(command.logId())
			.orElseThrow(() -> new MeetingLogNotFoundException(command.logId()));
		validateAuthor(log, command.userId());
		MeetingLogCommandValidator.validateForUpdate(command.body(), command.photos());

		log.edit(command.body(), Instant.now());
		meetingLogRepository.save(log);
		meetingLogPhotoRepository.deleteByLogId(log.getId());
		meetingLogPhotoRepository.saveAll(toPhotos(log.getId(), command.photos(), log.getUpdatedAt()));
		return Result.of(log.getId(), log.getMeetingId());
	}

	private void validateAuthor(MeetingLog log, Long userId) {
		if (!log.getAuthorUserId().equals(userId)) {
			throw new AccessDeniedException("작성자 본인만 방탈로그를 수정할 수 있습니다.");
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
