package com.bangpot.meeting.application.service;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.common.storage.FileStorage;
import com.bangpot.meeting.application.exception.MeetingLogRequestValidationException;
import com.bangpot.meeting.application.usecase.UploadMeetingLogPhotoUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadMeetingLogPhotoService implements UploadMeetingLogPhotoUseCase {

	private static final long MAX_PHOTO_SIZE_BYTES = 5L * 1024 * 1024;
	private static final String LOG_PHOTO_CATEGORY = "log-photos";

	private final FileStorage fileStorage;
	private final CompletedUserAccessService completedUserAccessService;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "log photo upload requires a completed user");

		MultipartFile file = command.file();
		validate(file);

		var storedFile = fileStorage.store(command.baseUrl(), LOG_PHOTO_CATEGORY, file);
		return Result.of(storedFile.url(), storedFile.sizeBytes());
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new MeetingLogRequestValidationException(List.of(
				new ApiErrorField("file", "사진 파일은 비어 있을 수 없습니다.")
			));
		}
		String extension = extractExtension(file.getOriginalFilename());
		if (!List.of("jpg", "jpeg", "png").contains(extension)) {
			throw new MeetingLogRequestValidationException(List.of(
				new ApiErrorField("file", "사진은 jpg, jpeg, png 형식만 허용됩니다.")
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

