package com.bangpot.meeting.application.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.common.storage.FileStorage;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.exception.MeetingLogWriteNotAllowedException;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.UploadMeetingLogPhotoUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadMeetingLogPhotoService implements UploadMeetingLogPhotoUseCase {

	private static final long MAX_PHOTO_SIZE_BYTES = 5L * 1024 * 1024;
	private static final String LOG_PHOTO_CATEGORY = "log-photos";
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
	private static final String COMPLETED_USER_REQUIRED_MESSAGE = "방탈로그 사진 업로드는 가입 완료 사용자만 가능합니다.";
	private static final String PARTICIPATION_HISTORY_REQUIRED_MESSAGE = "완료된 모임에 참여한 사용자만 방탈로그 사진을 업로드할 수 있습니다.";

	private final FileStorage fileStorage;
	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), COMPLETED_USER_REQUIRED_MESSAGE);
		Meeting meeting = meetingRepository.findById(command.meetingId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		validateUploadableMeeting(meeting);
		validateParticipantHistory(meeting, command.userId());

		MultipartFile file = command.file();
		validate(file);

		var storedFile = fileStorage.store(command.baseUrl(), LOG_PHOTO_CATEGORY, file);
		return Result.of(storedFile.url(), storedFile.sizeBytes());
	}

	private void validateUploadableMeeting(Meeting meeting) {
		if (meeting.getStatus() != MeetingStatus.COMPLETED) {
			throw new MeetingLogWriteNotAllowedException(meeting.getId());
		}
	}

	private void validateParticipantHistory(Meeting meeting, Long userId) {
		if (meeting.getHostUserId().equals(userId)) {
			return;
		}
		boolean hasHistory = meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), userId)
			.map(participant -> participant.getStatus().representsJoined())
			.orElse(false);
		if (!hasHistory) {
			throw new AccessDeniedException(PARTICIPATION_HISTORY_REQUIRED_MESSAGE);
		}
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new MeetingLogRequestValidationException(List.of(
				new ApiErrorField("file", "사진 파일은 비어 있을 수 없습니다.")
			));
		}
		String extension = extractExtension(file.getOriginalFilename());
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new MeetingLogRequestValidationException(List.of(
				new ApiErrorField("file", "사진은 jpg, jpeg, png 형식만 허용됩니다.")
			));
		}
		if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
			throw new MeetingLogRequestValidationException(List.of(
				new ApiErrorField("file.contentType", "사진은 jpg, jpeg, png 형식만 허용됩니다.")
			));
		}
		if (file.getSize() > MAX_PHOTO_SIZE_BYTES) {
			throw new MeetingLogRequestValidationException(List.of(
				new ApiErrorField("file.sizeBytes", "사진은 5MB를 초과할 수 없습니다.")
			));
		}
	}

	private String extractExtension(String originalFilename) {
		if (originalFilename == null) {
			return "";
		}
		int dotIndex = originalFilename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
			return "";
		}
		return originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
	}

}
