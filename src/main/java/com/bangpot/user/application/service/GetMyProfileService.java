package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.domain.view.MyProfileView;
import com.bangpot.user.domain.view.UserProfileView;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyProfileService implements GetMyProfileUseCase {

	private final UserQueryRepository userQueryRepository;
	private final MeetingQueryRepository meetingQueryRepository;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	public MyProfileView handle(Query query) {
		UserProfileView user = userQueryRepository.findMyProfileUserViewByUserId(query.userId());
		if (user == null) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}

		return MyProfileView.of(
			user.id(),
			user.nickname(),
			user.profileImageUrl(),
			meetingQueryRepository.countCreatedByHostUserId(query.userId()),
			meetingQueryRepository.countJoinedByUserId(query.userId()),
			crewQueryRepository.countActiveByMemberUserId(query.userId()),
			crewQueryRepository.countPendingPublicByUserId(query.userId())
		);
	}
}
