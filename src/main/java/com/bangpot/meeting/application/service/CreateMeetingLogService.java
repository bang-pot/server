package com.bangpot.meeting.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.image.application.usecase.AttachImageUploadUseCase;
import com.bangpot.image.domain.ImageUploadCategory;
import com.bangpot.meeting.application.exception.MeetingLogAlreadyExistsException;
import com.bangpot.meeting.application.exception.MeetingLogWriteNotAllowedException;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingLogPhotoRepository;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.MeetingLogPhoto;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMeetingLogService implements CreateMeetingLogUseCase {

	private static final String COMPLETED_USER_REQUIRED_MESSAGE = "완료된 사용자만 방탈로그를 작성할 수 있습니다.";
	private static final String PARTICIPATION_HISTORY_REQUIRED_MESSAGE =
		"완료된 모임에 참여한 사용자만 방탈로그를 작성할 수 있습니다.";
	private static final String LOG_PHOTO_DIRECTORY = "log-photos";
	private static final Set<MeetingParticipationStatus> WRITABLE_HISTORY_STATUSES = Set.of(
		MeetingParticipationStatus.JOINED,
		MeetingParticipationStatus.PENDING,
		MeetingParticipationStatus.APPROVED
	);

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final MeetingLogRepository meetingLogRepository;
	private final MeetingLogPhotoRepository meetingLogPhotoRepository;
	private final AttachImageUploadUseCase attachImageUploadUseCase;
	private final Clock clock;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), COMPLETED_USER_REQUIRED_MESSAGE);
		Meeting meeting = meetingRepository.findById(command.meetingId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		validateWritableMeeting(meeting);
		validateParticipantHistory(meeting, command.userId());
		if (meetingLogRepository.existsAnyByMeetingIdAndAuthorUserId(command.meetingId(), command.userId())) {
			throw new MeetingLogAlreadyExistsException(command.meetingId(), command.userId());
		}

		MeetingLogCommandValidator.validate(command.body(), command.photos());

		Instant now = clock.instant();
		MeetingLog savedLog = saveLog(command, now);
		List<String> photoUrls = attachPhotos(command);
		meetingLogPhotoRepository.saveAll(toPhotos(savedLog.getId(), photoUrls, now));
		return Result.of(savedLog.getId(), savedLog.getMeetingId());
	}

	private void validateWritableMeeting(Meeting meeting) {
		if (meeting.getStatus() != MeetingStatus.COMPLETED) {
			throw new MeetingLogWriteNotAllowedException(meeting.getId());
		}
	}

	private void validateParticipantHistory(Meeting meeting, Long userId) {
		if (meeting.getHostUserId().equals(userId)) {
			return;
		}
		boolean hasHistory = meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), userId)
			.map(participant -> WRITABLE_HISTORY_STATUSES.contains(participant.getStatus()))
			.orElse(false);
		if (!hasHistory) {
			throw new AccessDeniedException(PARTICIPATION_HISTORY_REQUIRED_MESSAGE);
		}
	}

	private MeetingLog saveLog(Command command, Instant now) {
		try {
			return meetingLogRepository.save(
				MeetingLog.create(command.meetingId(), command.userId(), command.body(), now)
			);
		} catch (DataIntegrityViolationException exception) {
			throw new MeetingLogAlreadyExistsException(command.meetingId(), command.userId(), exception);
		}
	}

	private List<String> attachPhotos(Command command) {
		return attachImageUploadUseCase.handle(AttachImageUploadUseCase.Command.of(
			command.userId(),
			ImageUploadCategory.MEETING_LOG_PHOTO,
			LOG_PHOTO_DIRECTORY,
			uploadIds(command.photos())
		)).urls();
	}

	private List<Long> uploadIds(List<PhotoInput> photos) {
		if (photos == null || photos.isEmpty()) {
			return List.of();
		}
		return photos.stream()
			.map(PhotoInput::uploadId)
			.toList();
	}

	private List<MeetingLogPhoto> toPhotos(Long logId, List<String> photoUrls, Instant now) {
		if (photoUrls == null || photoUrls.isEmpty()) {
			return List.of();
		}
		return photoUrls.stream()
			.map(url -> MeetingLogPhoto.create(logId, url, now))
			.toList();
	}
}
