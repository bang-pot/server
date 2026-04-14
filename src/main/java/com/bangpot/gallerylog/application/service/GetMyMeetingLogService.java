package com.bangpot.gallerylog.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bangpot.gallerylog.application.exception.MeetingLogNotFoundException;
import com.bangpot.gallerylog.application.port.MeetingLogPhotoRepository;
import com.bangpot.gallerylog.application.port.MeetingLogRepository;
import com.bangpot.gallerylog.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.gallerylog.domain.MeetingLog;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyMeetingLogService implements GetMyMeetingLogUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final MeetingRepository meetingRepository;
	private final MeetingLogRepository meetingLogRepository;
	private final MeetingLogPhotoRepository meetingLogPhotoRepository;
	private final UserRepository userRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "방탈로그를 조회할 수 없습니다.");
		Meeting meeting = meetingRepository.findById(query.meetingId())
			.orElseThrow(() -> new MeetingNotFoundException(query.meetingId()));
		MeetingLog log = meetingLogRepository.findByMeetingIdAndAuthorUserId(query.meetingId(), query.userId())
			.orElseThrow(() -> new MeetingLogNotFoundException(query.meetingId()));
		return toResult(log, meeting);
	}

	Result toResult(MeetingLog log, Meeting meeting) {
		String nickname = userRepository.findById(log.getAuthorUserId())
			.orElseThrow(() -> new UserNotFoundException(log.getAuthorUserId()))
			.getNickname();
		List<String> photos = meetingLogPhotoRepository.findAllByLogId(log.getId()).stream()
			.map(photo -> photo.getPhotoUrl())
			.toList();
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
			photos
		);
	}
}
