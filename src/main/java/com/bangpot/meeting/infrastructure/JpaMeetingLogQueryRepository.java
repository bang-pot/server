package com.bangpot.meeting.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.meeting.application.port.MeetingLogQueryRepository;
import com.bangpot.meeting.domain.view.MeetingLogDetailView;
import com.bangpot.meeting.domain.view.MyMeetingLogView;
import com.bangpot.meeting.domain.view.MyMeetingLogsView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaMeetingLogQueryRepository implements MeetingLogQueryRepository {

	private static final int EXCERPT_LIMIT = 120;

	private final MeetingLogJpaRepository meetingLogJpaRepository;

	@Override
	public MyMeetingLogsView findMyMeetingLogsViewByAuthorUserId(Long userId, int page, int size) {
		org.springframework.data.domain.Slice<MyMeetingLogsView.Item> slice =
			meetingLogJpaRepository.findMyMeetingLogsViewByAuthorUserId(
			userId,
			CrewStatus.ACTIVE,
			EXCERPT_LIMIT,
			PageRequest.of(page, size)
			);
		return MyMeetingLogsView.of(slice.getContent(), MyMeetingLogsView.Page.of(page, size, slice.hasNext()));
	}

	@Override
	public boolean existsMeetingById(Long meetingId) {
		return meetingLogJpaRepository.existsMeetingById(meetingId);
	}

	@Override
	public Optional<MyMeetingLogView> findMyMeetingLogView(
		Long meetingId,
		Long authorUserId
	) {
		return meetingLogJpaRepository.findMyMeetingLogSource(meetingId, authorUserId)
			.map(source -> {
				List<String> photos = meetingLogJpaRepository.findPhotoUrlsByLogId(source.logId());
				return MyMeetingLogView.of(
					source.logId(),
					source.meetingId(),
					source.meetingTitle(),
					source.themeName(),
					source.place(),
					source.date(),
					source.authorNickname(),
					source.createdAt(),
					source.updatedAt(),
					source.body(),
					photos
				);
			});
	}

	@Override
	public Optional<MeetingLogDetailView> findMeetingLogDetailView(Long crewId, Long logId) {
		return meetingLogJpaRepository.findMeetingLogDetailSource(crewId, logId)
			.map(source -> {
				List<String> photos = meetingLogJpaRepository.findPhotoUrlsByLogId(source.logId());
				return MeetingLogDetailView.of(
					source.logId(),
					source.meetingId(),
					source.meetingTitle(),
					source.themeName(),
					source.place(),
					source.date(),
					source.authorNickname(),
					source.createdAt(),
					source.updatedAt(),
					source.body(),
					photos
				);
			});
	}

	@Override
	public boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
		return meetingLogJpaRepository.existsByMeetingIdAndAuthorUserIdAndDeletedAtIsNotNull(meetingId, authorUserId);
	}
}
