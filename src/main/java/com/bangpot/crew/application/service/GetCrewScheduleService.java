package com.bangpot.crew.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewScheduleUseCase;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.service.MeetingAutomaticTransitionService;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewScheduleService implements GetCrewScheduleUseCase {

	private static final List<MeetingStatus> INCLUDED_STATUSES = List.of(
		MeetingStatus.RECRUITING,
		MeetingStatus.RECRUITMENT_CLOSED,
		MeetingStatus.COMPLETED,
		MeetingStatus.CANCELED
	);

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final MeetingAutomaticTransitionService meetingAutomaticTransitionService;

	@Override
	@Transactional(readOnly = true)
	public Result handle(Query query) {
		crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		completedUserAccessService.validateCompletedUser(query.userId(), "Only joined crew members can read crew schedule.");
		if (crewMemberRepository.findByCrewIdAndUserId(query.crewId(), query.userId()).isEmpty()) {
			throw new AccessDeniedException("Only joined crew members can read crew schedule.");
		}

		LocalDate from = LocalDate.parse(query.from());
		LocalDate to = LocalDate.parse(query.to());

		List<Item> items = meetingRepository.findAllByCrewId(query.crewId()).stream()
			.map(meetingAutomaticTransitionService::apply)
			.filter(meeting -> INCLUDED_STATUSES.contains(meeting.getStatus()))
			.filter(meeting -> isWithinRange(meeting, from, to))
			.map(this::toItem)
			.toList();

		return Result.of(items);
	}

	private boolean isWithinRange(Meeting meeting, LocalDate from, LocalDate to) {
		LocalDate meetingDate = LocalDate.parse(meeting.getMeetingDate());
		return !meetingDate.isBefore(from) && !meetingDate.isAfter(to);
	}

	private Item toItem(Meeting meeting) {
		return Item.of(
			meeting.getId(),
			meeting.getThemeName(),
			meeting.getMeetingDate(),
			meeting.getMeetingTime(),
			meeting.getStatus().name(),
			meeting.getStatus() == MeetingStatus.RECRUITING ? "OPEN" : "CLOSED",
			meeting.getPlace(),
			meetingParticipantRepository.countByMeetingId(meeting.getId()) + 1L,
			meeting.getStatus() == MeetingStatus.CANCELED
		);
	}
}
