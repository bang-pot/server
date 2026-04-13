package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.meeting.application.exception.MeetingNotFoundException;
import com.bangpot.meeting.application.exception.MeetingParticipationAlreadyJoinedException;
import com.bangpot.meeting.application.exception.MeetingParticipationNotJoinedException;
import com.bangpot.meeting.application.exception.MeetingHostCannotCancelParticipationException;
import com.bangpot.meeting.application.exception.MeetingInvalidStatusTransitionException;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.service.CancelMeetingService;
import com.bangpot.meeting.application.service.CancelMeetingParticipationService;
import com.bangpot.meeting.application.service.CloseMeetingRecruitmentService;
import com.bangpot.meeting.application.service.CompleteMeetingService;
import com.bangpot.meeting.application.service.CreateMeetingService;
import com.bangpot.meeting.application.service.GetMeetingDetailService;
import com.bangpot.meeting.application.service.GetMeetingsService;
import com.bangpot.meeting.application.service.JoinMeetingService;
import com.bangpot.meeting.application.service.ReopenMeetingRecruitmentService;
import com.bangpot.meeting.application.usecase.CancelMeetingUseCase;
import com.bangpot.meeting.application.usecase.CancelMeetingParticipationUseCase;
import com.bangpot.meeting.application.usecase.CloseMeetingRecruitmentUseCase;
import com.bangpot.meeting.application.usecase.CompleteMeetingUseCase;
import com.bangpot.meeting.application.usecase.CreateMeetingUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingsUseCase;
import com.bangpot.meeting.application.usecase.JoinMeetingUseCase;
import com.bangpot.meeting.application.usecase.ReopenMeetingRecruitmentUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.MeetingResult;
import com.bangpot.meeting.domain.MeetingStatus;

class MeetingUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-04-12T10:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryMeetingRepository meetingRepository;
	private InMemoryMeetingParticipantRepository meetingParticipantRepository;
	private CreateMeetingUseCase createMeetingUseCase;
	private GetMeetingsUseCase getMeetingsUseCase;
	private GetMeetingDetailUseCase getMeetingDetailUseCase;
	private JoinMeetingUseCase joinMeetingUseCase;
	private CancelMeetingParticipationUseCase cancelMeetingParticipationUseCase;
	private CloseMeetingRecruitmentUseCase closeMeetingRecruitmentUseCase;
	private ReopenMeetingRecruitmentUseCase reopenMeetingRecruitmentUseCase;
	private CancelMeetingUseCase cancelMeetingUseCase;
	private CompleteMeetingUseCase completeMeetingUseCase;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingRepository = new InMemoryMeetingRepository();
		meetingParticipantRepository = new InMemoryMeetingParticipantRepository();
		createMeetingUseCase = new CreateMeetingService(authUserRepository, crewRepository, crewMemberRepository, meetingRepository);
		getMeetingsUseCase = new GetMeetingsService(authUserRepository, crewRepository, crewMemberRepository, meetingRepository);
		getMeetingDetailUseCase = new GetMeetingDetailService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository
		);
		joinMeetingUseCase = new JoinMeetingService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository
		);
		cancelMeetingParticipationUseCase = new CancelMeetingParticipationService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingParticipantRepository
		);
		closeMeetingRecruitmentUseCase = new CloseMeetingRecruitmentService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
		reopenMeetingRecruitmentUseCase = new ReopenMeetingRecruitmentService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
		cancelMeetingUseCase = new CancelMeetingService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
		completeMeetingUseCase = new CompleteMeetingService(
			authUserRepository,
			crewRepository,
			crewMemberRepository,
			meetingRepository
		);
	}

	@Test
	void createsMeetingWithDefaultStatusAndResultForJoinedMember() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		CreateMeetingUseCase.Result result = createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			crew.getId(),
			member.getId(),
			"2026-04-20",
			"19:30",
			"Gangnam",
			"Time Attack",
			4,
			120000,
			"https://example.com/reserve",
			"https://open.kakao.com/o/abc123",
			"Please arrive on time"
		));

		assertThat(result.meetingId()).isNotNull();
		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(result.result()).isEqualTo("NOT_RECORDED");

		Meeting saved = meetingRepository.findById(result.meetingId()).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(MeetingStatus.RECRUITING);
		assertThat(saved.getResult()).isEqualTo(MeetingResult.NOT_RECORDED);
		assertThat(saved.getHostUserId()).isEqualTo(member.getId());
	}

	@Test
	void returnsMeetingsForJoinedMember() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "Deep Blue", "Hongdae", "2026-04-21", "20:00", 6, null, null, null, null
		));
		meetingRepository.save(Meeting.create(
			crew.getId(), member.getId(), "Time Attack", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		List<GetMeetingsUseCase.View> result = getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(crew.getId(), member.getId()));

		assertThat(result).hasSize(2);
		assertThat(result.get(0).themeName()).isEqualTo("Time Attack");
		assertThat(result.get(0).status()).isEqualTo("RECRUITING");
		assertThat(result.get(1).themeName()).isEqualTo("Deep Blue");
	}

	@Test
	void returnsMeetingDetailForHostAsJoined() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			member.getId(),
			"Time Attack",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			120000,
			"https://example.com/reserve",
			"https://open.kakao.com/o/abc123",
			"Please arrive on time"
		));

		GetMeetingDetailUseCase.Result result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.hostUserId()).isEqualTo(member.getId());
		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(result.result()).isEqualTo("NOT_RECORDED");
		assertThat(result.myParticipationStatus()).isEqualTo("JOINED");
	}

	@Test
	void returnsNotJoinedWhenCurrentUserHasNotJoined() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Test Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));

		GetMeetingDetailUseCase.Result result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.myParticipationStatus()).isEqualTo("NOT_JOINED");
	}

	@Test
	void returnsJoinedWhenCurrentUserHasJoinedMeeting() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Test Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), member.getId()));

		GetMeetingDetailUseCase.Result result = getMeetingDetailUseCase.handle(
			GetMeetingDetailUseCase.Query.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.myParticipationStatus()).isEqualTo("JOINED");
	}

	@Test
	void joinsMeetingImmediatelyForJoinedCrewMember() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Test Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));

		JoinMeetingUseCase.Result result = joinMeetingUseCase.handle(
			JoinMeetingUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.myParticipationStatus()).isEqualTo("JOINED");
		assertThat(meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), member.getId()))
			.isPresent()
			.get()
			.extracting(MeetingParticipant::getStatus)
			.isEqualTo(MeetingParticipationStatus.JOINED);
	}

	@Test
	void rejectsJoinWhenAlreadyJoined() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Test Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), member.getId()));

		assertThatThrownBy(() -> joinMeetingUseCase.handle(
			JoinMeetingUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		)).isInstanceOf(MeetingParticipationAlreadyJoinedException.class);
	}

	@Test
	void rejectsJoinForHostWhoIsAlreadyJoined() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Test Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));

		assertThatThrownBy(() -> joinMeetingUseCase.handle(
			JoinMeetingUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		)).isInstanceOf(MeetingParticipationAlreadyJoinedException.class);
	}

	@Test
	void cancelsJoinedParticipationForJoinedCrewMember() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(),
			host.getId(),
			"Test Theme",
			"Gangnam",
			"2026-04-20",
			"19:30",
			4,
			null,
			null,
			null,
			null
		));
		meetingParticipantRepository.save(MeetingParticipant.join(meeting.getId(), member.getId()));

		CancelMeetingParticipationUseCase.Result result = cancelMeetingParticipationUseCase.handle(
			CancelMeetingParticipationUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.myParticipationStatus()).isEqualTo("NOT_JOINED");
		assertThat(meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), member.getId())).isEmpty();
	}

	@Test
	void rejectsCancelWhenNotJoinedYet() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> cancelMeetingParticipationUseCase.handle(
			CancelMeetingParticipationUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		)).isInstanceOf(MeetingParticipationNotJoinedException.class);
	}

	@Test
	void rejectsCancelForMeetingHost() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> cancelMeetingParticipationUseCase.handle(
			CancelMeetingParticipationUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		)).isInstanceOf(MeetingHostCannotCancelParticipationException.class);
	}

	@Test
	void closesRecruitmentForMeetingHost() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		CloseMeetingRecruitmentUseCase.Result result = closeMeetingRecruitmentUseCase.handle(
			CloseMeetingRecruitmentUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		);

		assertThat(result.meetingId()).isEqualTo(meeting.getId());
		assertThat(result.status()).isEqualTo("RECRUITMENT_CLOSED");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITMENT_CLOSED);
	}

	@Test
	void reopensRecruitmentForMeetingHost() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meeting.closeRecruitment();

		ReopenMeetingRecruitmentUseCase.Result result = reopenMeetingRecruitmentUseCase.handle(
			ReopenMeetingRecruitmentUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		);

		assertThat(result.status()).isEqualTo("RECRUITING");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.RECRUITING);
	}

	@Test
	void cancelsMeetingForCrewLeader() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser leader = fullUser(78L, "leader-provider", "leader");
		authUserRepository.save(host);
		authUserRepository.save(leader);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), leader.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		CancelMeetingUseCase.Result result = cancelMeetingUseCase.handle(
			CancelMeetingUseCase.Command.of(crew.getId(), meeting.getId(), leader.getId())
		);

		assertThat(result.status()).isEqualTo("CANCELED");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.CANCELED);
	}

	@Test
	void completesMeetingForHostWhenRecruitmentIsClosed() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));
		meeting.closeRecruitment();

		CompleteMeetingUseCase.Result result = completeMeetingUseCase.handle(
			CompleteMeetingUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		);

		assertThat(result.status()).isEqualTo("COMPLETED");
		assertThat(meetingRepository.findById(meeting.getId())).get().extracting(Meeting::getStatus)
			.isEqualTo(MeetingStatus.COMPLETED);
	}

	@Test
	void rejectsMeetingCompletionFromRecruitingState() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		authUserRepository.save(host);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> completeMeetingUseCase.handle(
			CompleteMeetingUseCase.Command.of(crew.getId(), meeting.getId(), host.getId())
		)).isInstanceOf(MeetingInvalidStatusTransitionException.class);
	}

	@Test
	void rejectsRecruitmentCloseForNonHostMember() {
		AuthUser host = fullUser(77L, "host-provider", "host");
		AuthUser member = fullUser(78L, "member-provider", "member");
		authUserRepository.save(host);
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), host.getId()));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));
		Meeting meeting = meetingRepository.save(Meeting.create(
			crew.getId(), host.getId(), "Test Theme", "Gangnam", "2026-04-20", "19:30", 4, null, null, null, null
		));

		assertThatThrownBy(() -> closeMeetingRecruitmentUseCase.handle(
			CloseMeetingRecruitmentUseCase.Command.of(crew.getId(), meeting.getId(), member.getId())
		)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMeetingCreateForNonMember() {
		AuthUser outsider = fullUser(88L, "outsider-provider", "outsider");
		authUserRepository.save(outsider);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));

		assertThatThrownBy(() -> createMeetingUseCase.handle(CreateMeetingUseCase.Command.of(
			crew.getId(), outsider.getId(), "2026-04-20", "19:30", "Gangnam", "Time Attack", 4, null, null, null, null
		))).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMeetingAccessForTempUser() {
		AuthUser tempUser = tempUser(89L, "temp-provider");
		authUserRepository.save(tempUser);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), tempUser.getId()));

		assertThatThrownBy(() -> getMeetingsUseCase.handle(GetMeetingsUseCase.Query.of(crew.getId(), tempUser.getId())))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void rejectsMeetingDetailForUnknownMeeting() {
		AuthUser member = fullUser(77L, "member-provider", "member");
		authUserRepository.save(member);
		Crew crew = crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createMember(crew.getId(), member.getId()));

		assertThatThrownBy(() -> getMeetingDetailUseCase.handle(GetMeetingDetailUseCase.Query.of(crew.getId(), 999L, member.getId())))
			.isInstanceOf(MeetingNotFoundException.class);
	}

	private AuthUser fullUser(Long id, String providerId, String nickname) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.FULL,
			nickname,
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
	}

	private AuthUser tempUser(Long id, String providerId) {
		return AuthUser.rehydrate(
			id,
			AuthProvider.KAKAO,
			providerId,
			AuthUserStatus.TEMP,
			null,
			null,
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
	}

	private static final class InMemoryAuthUserRepository implements AuthUserRepository {
		private final Map<Long, AuthUser> usersById = new HashMap<>();

		@Override
		public Optional<AuthUser> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		@Override
		public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
			return usersById.values().stream()
				.filter(user -> user.getProvider() == provider && providerId.equals(user.getProviderId()))
				.findFirst();
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return usersById.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public AuthUser save(AuthUser user) {
			usersById.put(user.getId(), user);
			return user;
		}
	}

	private static final class InMemoryCrewRepository implements CrewRepository {
		private final Map<Long, Crew> crewsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public boolean existsByName(String name) {
			return crewsById.values().stream().anyMatch(crew -> name.equals(crew.getName()));
		}

		@Override
		public Crew save(Crew crew) {
			if (crew.getId() == null) {
				crew.assignId(sequence++);
			}
			crewsById.put(crew.getId(), crew);
			return crew;
		}

		@Override
		public Optional<Crew> findById(Long crewId) {
			return Optional.ofNullable(crewsById.get(crewId));
		}

		@Override
		public List<Crew> findPublicCrews() {
			return crewsById.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		private final Map<Long, CrewMember> membersById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewMember save(CrewMember crewMember) {
			if (crewMember.getId() == null) {
				crewMember.assignId(sequence++);
			}
			membersById.put(crewMember.getId(), crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId).isPresent();
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId)
				.map(member -> member.getRole() == CrewRole.LEADER)
				.orElse(false);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()) && userId.equals(member.getUserId()))
				.findFirst();
		}

		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return membersById.values().stream()
				.filter(member -> crewId.equals(member.getCrewId()))
				.toList();
		}
	}

	private static final class InMemoryMeetingRepository implements MeetingRepository {
		private final Map<Long, Meeting> meetingsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public Meeting save(Meeting meeting) {
			if (meeting.getId() == null) {
				meeting.assignId(sequence++);
			}
			meetingsById.put(meeting.getId(), meeting);
			return meeting;
		}

		@Override
		public List<Meeting> findAllByCrewId(Long crewId) {
			return meetingsById.values().stream()
				.filter(meeting -> crewId.equals(meeting.getCrewId()))
				.sorted(Comparator
					.comparing(Meeting::getMeetingDate)
					.thenComparing(Meeting::getMeetingTime)
					.thenComparing(Meeting::getId))
				.toList();
		}

		@Override
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetingsById.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return meetingsById.values().stream()
				.filter(meeting -> meetingId.equals(meeting.getId()) && crewId.equals(meeting.getCrewId()))
				.findFirst();
		}
	}

	private static final class InMemoryMeetingParticipantRepository implements MeetingParticipantRepository {
		private final Map<Long, MeetingParticipant> participantsById = new HashMap<>();
		private long sequence = 1L;

		@Override
		public MeetingParticipant save(MeetingParticipant participant) {
			if (participant.getId() == null) {
				participant.assignId(sequence++);
			}
			participantsById.put(participant.getId(), participant);
			return participant;
		}

		@Override
		public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
			return participantsById.values().stream()
				.filter(participant -> meetingId.equals(participant.getMeetingId()) && userId.equals(participant.getUserId()))
				.findFirst();
		}

		@Override
		public void delete(MeetingParticipant participant) {
			participantsById.remove(participant.getId());
		}
	}
}
