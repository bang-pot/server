package com.bangpot.meeting.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.exception.MeetingGalleryNotFoundException;
import com.bangpot.meeting.application.port.MeetingGalleryReadRepository;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryDetailUseCase;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMeetingGalleryDetailService implements GetCrewMeetingGalleryDetailUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingGalleryReadRepository meetingGalleryReadRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "모임 갤러리 상세 조회는 가입 완료 사용자만 가능합니다.");
		crewRepository.findById(query.crewId()).orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		if (!crewMemberRepository.existsByCrewIdAndUserId(query.crewId(), query.userId())) {
			throw new AccessDeniedException("meeting gallery detail access requires an active crew membership");
		}

		MeetingGalleryReadRepository.Detail detail = meetingGalleryReadRepository.findDetail(query.crewId(), query.meetingId())
			.orElseThrow(() -> new MeetingGalleryNotFoundException(query.meetingId()));

		List<Photo> photos = detail.photos().stream()
			.map(photo -> Photo.of(photo.photoId(), photo.url(), photo.order()))
			.toList();

		return Result.of(
			detail.meetingId(),
			detail.meetingDate(),
			detail.meetingTitle(),
			photos,
			detail.totalPhotoCount()
		);
	}
}
