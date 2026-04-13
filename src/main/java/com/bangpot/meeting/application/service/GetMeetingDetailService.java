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
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipationStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMeetingDetailService implements GetMeetingDetailUseCase {

	private final AuthUserRepository authUserRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;

	@Override
	@Transactional(readOnly = true)
	public Result handle(Query query) {
		crewRepository.findById(query.crewId()).orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("가입한 크루원만 모임 상세를 조회할 수 있습니다.");
		}
		if (crewMemberRepository.findByCrewIdAndUserId(query.crewId(), query.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 모임 상세를 조회할 수 있습니다.");
		}

		Meeting meeting = meetingRepository.findByIdAndCrewId(query.meetingId(), query.crewId())
			.orElseThrow(() -> new MeetingNotFoundException(query.meetingId()));

		String myParticipationStatus = meeting.getHostUserId().equals(query.userId())
			? MeetingParticipationStatus.JOINED.name()
			: meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), query.userId())
				.map(participant -> participant.getStatus().representsJoined()
					? MeetingParticipationStatus.JOINED.name()
					: MeetingParticipationStatus.NOT_JOINED.name())
				.orElse(MeetingParticipationStatus.NOT_JOINED.name());

		return Result.of(
			meeting.getId(),
			meeting.getCrewId(),
			meeting.getHostUserId(),
			meeting.getThemeName(),
			meeting.getPlace(),
			meeting.getMeetingDate(),
			meeting.getMeetingTime(),
			meeting.getCapacity(),
			meeting.getTotalCost(),
			meeting.getReservationLink(),
			meeting.getOpenChatLink(),
			meeting.getDescription(),
			meeting.getStatus().name(),
			meeting.getResult().name(),
			myParticipationStatus
		);
	}
}
