package com.bangpot.meeting.application.service;

import org.springframework.stereotype.Service;

import com.bangpot.meeting.application.exception.MeetingLogNotFoundException;
import com.bangpot.meeting.application.port.MeetingLogPhotoRepository;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMeetingLogDetailService implements GetMeetingLogDetailUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingLogRepository meetingLogRepository;
	private final MeetingLogPhotoRepository meetingLogPhotoRepository;
	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "meeting log detail access requires a completed user");

		MeetingLog log = meetingLogRepository.findById(query.logId())
			.orElseThrow(() -> new MeetingLogNotFoundException(query.logId()));
		Meeting meeting = meetingRepository.findById(log.getMeetingId())
			.orElseThrow(() -> new MeetingNotFoundException(log.getMeetingId()));
		String nickname = userRepository.findById(log.getAuthorUserId())
			.orElseThrow(() -> new IllegalStateException("user not found"))
			.getNickname();

		return Result.of(
			log.getId(),
			meeting.getId(),
			meeting.getTitle(),
			meeting.getThemeName(),
			meeting.getPlace(),
			meeting.getMeetingDate(),
			nickname,
			log.getCreatedAt(),
			log.getUpdatedAt(),
			log.getBody(),
			meetingLogPhotoRepository.findAllByLogId(log.getId()).stream()
				.map(photo -> photo.getPhotoUrl())
				.toList()
		);
	}
}

