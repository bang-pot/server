package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.exception.CrewRemoveMemberTargetNotAllowedException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.RemoveCrewMemberUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingStatus;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RemoveCrewMemberService implements RemoveCrewMemberUseCase {

	private static final String REMOVE_DENIED_MESSAGE = "현재 크루장만 일반 크루원을 강제 제거할 수 있습니다.";

	private final CompletedUserAccessService completedUserAccessService;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;

	@Override
	@Transactional
	public Result handle(Command command) {
		Crew crew = crewRepository.findById(command.crewId())
			.orElseThrow(() -> new CrewNotFoundException(command.crewId()));

		completedUserAccessService.validateCompletedUser(command.leaderUserId(), REMOVE_DENIED_MESSAGE);

		CrewMember currentLeader = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.leaderUserId())
			.orElseThrow(() -> new AccessDeniedException(REMOVE_DENIED_MESSAGE));
		if (currentLeader.getRole() != CrewRole.LEADER) {
			throw new AccessDeniedException(REMOVE_DENIED_MESSAGE);
		}

		CrewMember targetMember = crewMemberRepository.findByCrewIdAndUserId(crew.getId(), command.targetUserId())
			.orElseThrow(() -> new CrewRemoveMemberTargetNotAllowedException(crew.getId(), command.targetUserId()));
		if (targetMember.getRole() != CrewRole.MEMBER) {
			throw new CrewRemoveMemberTargetNotAllowedException(crew.getId(), command.targetUserId());
		}

		List<Meeting> crewMeetings = meetingRepository.findAllByCrewId(crew.getId());
		for (Meeting meeting : crewMeetings) {
			if (meeting.getHostUserId().equals(command.targetUserId())
				&& (meeting.getStatus() == MeetingStatus.RECRUITING
					|| meeting.getStatus() == MeetingStatus.RECRUITMENT_CLOSED)) {
				meeting.cancel();
				meetingRepository.save(meeting);
				continue;
			}

			if (meeting.getStatus() != MeetingStatus.RECRUITING
				&& meeting.getStatus() != MeetingStatus.RECRUITMENT_CLOSED) {
				continue;
			}

			meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), command.targetUserId())
				.filter(participant -> participant.getStatus().representsJoined())
				.ifPresent(participant -> {
					participant.leave();
					meetingParticipantRepository.save(participant);
				});
		}

		targetMember.remove();
		crewMemberRepository.save(targetMember);
		return Result.of(crew.getId(), command.targetUserId());
	}
}
