package com.bangpot.meeting.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bangpot.meeting.application.port.MeetingLogPhotoRepository;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.meeting.domain.MeetingLog;
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
		completedUserAccessService.validateCompletedUser(query.userId(), "내 방탈로그 조회는 가입 완료 사용자만 가능합니다.");
		Meeting meeting = meetingRepository.findById(query.meetingId())
			.orElseThrow(() -> new MeetingNotFoundException(query.meetingId()));
		return meetingLogRepository.findByMeetingIdAndAuthorUserId(query.meetingId(), query.userId())
			.map(log -> toResult(log, meeting))
			.orElseGet(() -> toMissingResult(query));
	}

	Result toResult(MeetingLog log, Meeting meeting) {
		String nickname = userRepository.findById(log.getAuthorUserId())
			.orElseThrow(() -> new UserNotFoundException(log.getAuthorUserId()))
			.getNickname();
		List<String> photos = meetingLogPhotoRepository.findAllByLogId(log.getId()).stream()
			.map(photo -> photo.getPhotoUrl())
			.toList();
		return Result.of(
			Status.EXISTS,
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

	private Result toMissingResult(Query query) {
		if (meetingLogRepository.existsDeletedByMeetingIdAndAuthorUserId(query.meetingId(), query.userId())) {
			return Result.deletedBlocked();
		}
		return Result.notWritten();
	}
}
