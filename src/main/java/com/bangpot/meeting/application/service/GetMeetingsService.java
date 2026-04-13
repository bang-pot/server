package com.bangpot.meeting.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMeetingsService implements GetMeetingsUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingAutomaticTransitionService meetingAutomaticTransitionService;

	@Override
	@Transactional
	public List<View> handle(Query query) {
		crewRepository.findById(query.crewId()).orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("가입한 크루원만 모임 목록을 조회할 수 있습니다.");
		}
		if (crewMemberRepository.findByCrewIdAndUserId(query.crewId(), query.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 모임 목록을 조회할 수 있습니다.");
		}

		return meetingRepository.findAllByCrewId(query.crewId()).stream()
			.map(meetingAutomaticTransitionService::apply)
			.map(meeting -> View.of(
				meeting.getId(),
				meeting.getThemeName(),
				meeting.getPlace(),
				meeting.getMeetingDate(),
				meeting.getMeetingTime(),
				meeting.getStatus().name(),
				meeting.getResult().name(),
				meeting.getCapacity()
			))
			.toList();
	}
}
