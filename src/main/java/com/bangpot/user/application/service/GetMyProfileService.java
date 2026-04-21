package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyProfileService implements GetMyProfileUseCase {

	private final UserRepository userRepository;
	private final MeetingRepository meetingRepository;
	private final CrewRepository crewRepository;

	@Override
	public View handle(Query query) {
		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new AccessDeniedException("프로필 완료가 필요합니다."));
		return View.of(
			user.getId(),
			user.getNickname(),
			null,
			meetingRepository.countCreatedByHostUserId(query.userId()),
			meetingRepository.countJoinedByUserId(query.userId()),
			crewRepository.countActiveByMemberUserId(query.userId()),
			crewRepository.countPendingPublicByUserId(query.userId())
		);
	}
}
