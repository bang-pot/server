package com.bangpot.meeting.application.service;

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
import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.domain.Meeting;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMeetingService implements CreateMeetingUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		crewRepository.findById(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		AuthUser authUser = authUserRepository.findById(command.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(command.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("가입한 크루원만 모임을 생성할 수 있습니다.");
		}
		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 모임을 생성할 수 있습니다.");
		}

		Meeting meeting = meetingRepository.save(Meeting.create(
			command.crewId(),
			command.userId(),
			command.themeName(),
			command.place(),
			command.date(),
			command.time(),
			command.capacity(),
			command.totalCost(),
			command.reservationLink(),
			command.openChatLink(),
			command.description()
		));

		return Result.of(
			meeting.getId(),
			meeting.getCrewId(),
			meeting.getThemeName(),
			meeting.getPlace(),
			meeting.getMeetingDate(),
			meeting.getMeetingTime(),
			meeting.getStatus().name(),
			meeting.getResult().name()
		);
	}
}
