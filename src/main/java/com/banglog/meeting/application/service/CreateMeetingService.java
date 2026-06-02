package com.banglog.meeting.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.exception.CrewNotFoundException;
import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.usecase.CreateMeetingUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMeetingService implements CreateMeetingUseCase {

	private static final String CREATE_MEETING_DENIED_MESSAGE = "가입한 크루원만 모임을 생성할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		completedUserAccessService.validateCompletedUser(command.userId(), CREATE_MEETING_DENIED_MESSAGE);
		crewRepository.findByIdForShare(command.crewId()).orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		if (crewMemberRepository.findByCrewIdAndUserId(command.crewId(), command.userId()).isEmpty()) {
			throw new AccessDeniedException(CREATE_MEETING_DENIED_MESSAGE);
		}

		Meeting meeting = meetingRepository.save(Meeting.create(
			command.crewId(),
			command.userId(),
			command.title(),
			command.themeName(),
			command.place(),
			command.date(),
			command.time(),
			command.capacity(),
			command.totalCost(),
			command.contactLink(),
			command.description()
		));

		return Result.of(
			meeting.getId(),
			meeting.getCrewId(),
			meeting.getTitle(),
			meeting.getThemeName(),
			meeting.getPlace(),
			meeting.getMeetingDate(),
			meeting.getMeetingTime(),
			meeting.getStatus().name()
		);
	}
}
