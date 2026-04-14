package com.bangpot.gallerylog.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bangpot.gallerylog.application.exception.MeetingLogAlreadyExistsException;
import com.bangpot.gallerylog.application.port.MeetingLogPhotoRepository;
import com.bangpot.gallerylog.application.port.MeetingLogRepository;
import com.bangpot.gallerylog.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.gallerylog.domain.MeetingLog;
import com.bangpot.gallerylog.domain.MeetingLogPhoto;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMeetingLogService implements CreateMeetingLogUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final MeetingLogRepository meetingLogRepository;
	private final MeetingLogPhotoRepository meetingLogPhotoRepository;
	private final UserRepository userRepository;

	@Override
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), "방탈로그를 작성할 수 없습니다.");
		Meeting meeting = meetingRepository.findById(command.meetingId())
			.orElseThrow(() -> new MeetingNotFoundException(command.meetingId()));
		MeetingLogAccessPolicy.validateWritableMeeting(meeting);
		MeetingLogAccessPolicy.validateParticipantHistory(meeting, command.userId(), meetingParticipantRepository);
		if (meetingLogRepository.existsByMeetingIdAndAuthorUserId(command.meetingId(), command.userId())) {
			throw new MeetingLogAlreadyExistsException(command.meetingId(), command.userId());
		}

		MeetingLogCommandValidator.validate(command.body(), command.photos());
		userRepository.findById(command.userId()).orElseThrow(() -> new IllegalStateException("user not found"));

		Instant now = Instant.now();
		MeetingLog savedLog = meetingLogRepository.save(
			MeetingLog.create(command.meetingId(), command.userId(), command.body(), now)
		);
		meetingLogPhotoRepository.saveAll(toPhotos(savedLog.getId(), command.photos(), now));
		return Result.of(savedLog.getId(), savedLog.getMeetingId());
	}

	private List<MeetingLogPhoto> toPhotos(Long logId, List<PhotoInput> photos, Instant now) {
		if (photos == null || photos.isEmpty()) {
			return List.of();
		}
		return photos.stream()
			.map(photo -> MeetingLogPhoto.create(logId, photo.url(), now))
			.toList();
	}
}
