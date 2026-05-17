package com.bangpot.meeting.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;

import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.image.application.usecase.AttachImageUploadUseCase;
import com.bangpot.meeting.application.port.MeetingLogPhotoRepository;
import com.bangpot.meeting.application.port.MeetingLogQueryRepository;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.service.CreateMeetingLogService;
import com.bangpot.meeting.application.service.DeleteMeetingLogService;
import com.bangpot.meeting.application.service.GetMeetingLogDetailService;
import com.bangpot.meeting.application.service.GetMyMeetingLogService;
import com.bangpot.meeting.application.service.MeetingAccessService;
import com.bangpot.meeting.application.service.UpdateMeetingLogService;
import com.bangpot.meeting.application.usecase.CreateMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.DeleteMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.GetMeetingLogDetailUseCase;
import com.bangpot.meeting.application.usecase.GetMyMeetingLogUseCase;
import com.bangpot.meeting.application.usecase.UpdateMeetingLogUseCase;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.MeetingLog;
import com.bangpot.meeting.domain.MeetingLogPhoto;
import com.bangpot.meeting.domain.MeetingParticipant;
import com.bangpot.meeting.domain.MeetingParticipationStatus;
import com.bangpot.meeting.domain.view.MeetingLogDetailView;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.meeting.domain.view.MyMeetingLogView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

abstract class AbstractMeetingLogServicesTest {

	protected static final Instant NOW = Instant.parse("2026-04-14T12:00:00Z");

	protected InMemoryUserRepository userRepository;
	protected CompletedUserAccessService completedUserAccessService;
	protected InMemoryMeetingRepository meetingRepository;
	protected InMemoryCrewRepository crewRepository;
	protected InMemoryCrewMemberRepository crewMemberRepository;
	protected InMemoryMeetingParticipantRepository meetingParticipantRepository;
	protected InMemoryMeetingLogRepository meetingLogRepository;
	protected InMemoryMeetingLogPhotoRepository meetingLogPhotoRepository;
	protected AttachImageUploadUseCase attachImageUploadUseCase;
	protected InMemoryMeetingQueryRepository meetingQueryRepository;
	protected MeetingAccessService meetingAccessService;
	protected InMemoryMeetingLogQueryRepository meetingLogQueryRepository;
	protected CreateMeetingLogUseCase createMeetingLogUseCase;
	protected UpdateMeetingLogUseCase updateMeetingLogUseCase;
	protected DeleteMeetingLogUseCase deleteMeetingLogUseCase;
	protected GetMyMeetingLogUseCase getMyMeetingLogUseCase;
	protected GetMeetingLogDetailUseCase getMeetingLogDetailUseCase;

	@BeforeEach
	void setUp() {
		userRepository = new InMemoryUserRepository();
		completedUserAccessService = new CompletedUserAccessService(userRepository);
		meetingRepository = new InMemoryMeetingRepository();
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		meetingParticipantRepository = new InMemoryMeetingParticipantRepository();
		meetingLogRepository = new InMemoryMeetingLogRepository(meetingRepository);
		meetingLogPhotoRepository = new InMemoryMeetingLogPhotoRepository();
		attachImageUploadUseCase = mock(AttachImageUploadUseCase.class);
		lenient().when(attachImageUploadUseCase.handle(any()))
			.thenAnswer(invocation -> {
				AttachImageUploadUseCase.Command command = invocation.getArgument(0);
				return AttachImageUploadUseCase.Result.of(command.uploadIds().stream()
					.map(uploadId -> "https://cdn.example.com/upload-" + uploadId + ".jpg")
					.toList());
			});
		meetingQueryRepository = new InMemoryMeetingQueryRepository(crewRepository, crewMemberRepository);
		meetingAccessService = new MeetingAccessService(meetingQueryRepository);
		meetingLogQueryRepository = new InMemoryMeetingLogQueryRepository(
			meetingRepository,
			meetingLogRepository,
			meetingLogPhotoRepository,
			userRepository
		);
		createMeetingLogUseCase = new CreateMeetingLogService(
			completedUserAccessService,
			meetingRepository,
			meetingParticipantRepository,
			meetingLogRepository,
			meetingLogPhotoRepository,
			attachImageUploadUseCase,
			Clock.fixed(NOW, ZoneOffset.UTC)
		);
		updateMeetingLogUseCase = new UpdateMeetingLogService(
			completedUserAccessService,
			meetingLogRepository,
			meetingLogPhotoRepository,
			attachImageUploadUseCase,
			Clock.fixed(NOW, ZoneOffset.UTC)
		);
		deleteMeetingLogUseCase = new DeleteMeetingLogService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingLogRepository,
			Clock.fixed(NOW, ZoneOffset.UTC)
		);
		getMyMeetingLogUseCase = new GetMyMeetingLogService(
			completedUserAccessService,
			meetingLogQueryRepository
		);
		getMeetingLogDetailUseCase = new GetMeetingLogDetailService(
			completedUserAccessService,
			meetingAccessService,
			meetingLogQueryRepository
		);
	}

	protected User completedUser(Long id, String nickname) {
		User user = User.create(id, nickname);
		userRepository.save(user);
		return user;
	}

	protected User incompleteUser(Long id, String nickname) {
		return User.create(id, nickname);
	}

	protected Meeting completedMeeting(Long crewId, Long hostUserId, String themeName) {
		crewRepository.findById(crewId)
			.orElseGet(() -> crewRepository.save(Crew.create("Crew " + crewId, "desc", CrewVisibility.PUBLIC, null)));
		crewMemberRepository.save(CrewMember.createLeader(crewId, hostUserId));

		Meeting meeting = Meeting.create(
			crewId,
			hostUserId,
			themeName,
			themeName,
			"Hongdae",
			"2026-04-10",
			"20:00",
			4,
			100000,
			"https://open.kakao.com/o/test",
			"completed"
		);
		meetingRepository.save(meeting);
		meeting.closeRecruitment();
		meeting.complete();
		meetingRepository.save(meeting);
		return meeting;
	}

	protected Meeting recruitingMeeting(Long crewId, Long hostUserId, String themeName) {
		Meeting meeting = Meeting.create(
			crewId,
			hostUserId,
			themeName,
			themeName,
			"Hongdae",
			"2026-04-20",
			"20:00",
			4,
			100000,
			"https://open.kakao.com/o/test",
			"recruiting"
		);
		return meetingRepository.save(meeting);
	}

	protected void participant(Long meetingId, Long userId, MeetingParticipationStatus status) {
		meetingParticipantRepository.save(
			MeetingParticipant.rehydrate(null, meetingId, userId, status, NOW.minusSeconds(120), NOW.minusSeconds(120))
		);
	}

	protected void activeCrewMember(Long crewId, Long userId) {
		crewRepository.findById(crewId)
			.orElseGet(() -> crewRepository.save(Crew.create("Crew " + crewId, "desc", CrewVisibility.PUBLIC, null)));
		crewMemberRepository.save(CrewMember.createMember(crewId, userId));
	}

	protected static final class InMemoryUserRepository implements UserRepository {
		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			return false;
		}

		private final Map<Long, User> users = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(users.get(userId));
		}

		@Override
		public boolean existsByNickname(String nickname) {
			return users.values().stream().anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			return users.values().stream()
				.filter(user -> user.getNickname().contains(nickname))
				.toList();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}
	}

	protected static final class InMemoryCrewRepository implements CrewRepository {
		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return com.bangpot.crew.domain.view.MyCrewsView.of(
				java.util.List.of(),
				com.bangpot.crew.domain.view.MyCrewsView.Page.of(page, size, false)
			);
		}

		private final Map<Long, Crew> crews = new HashMap<>();
		private long sequence = 1L;

		@Override
		public boolean existsByName(String name) {
			return crews.values().stream().anyMatch(crew -> crew.getName().equals(name));
		}

		@Override
		public Crew save(Crew crew) {
			if (crew.getId() == null) {
				crew.assignId(sequence++);
			}
			crews.put(crew.getId(), crew);
			return crew;
		}

		@Override
		public Optional<Crew> findById(Long crewId) {
			return Optional.ofNullable(crews.get(crewId))
				.filter(crew -> crew.getStatus() == com.bangpot.crew.domain.CrewStatus.ACTIVE);
		}

		@Override
		public Optional<Crew> findByIdForUpdate(Long crewId) {
			return findById(crewId);
		}

		@Override
		public Optional<Crew> findByIdForShare(Long crewId) {
			return findById(crewId);
		}

		@Override
		public Optional<Crew> findAnyById(Long crewId) {
			return Optional.ofNullable(crews.get(crewId));
		}

		@Override
		public List<Crew> findActiveByMemberUserId(Long userId) {
			return List.of();
		}



				@Override
		public long countActiveByMemberUserId(Long userId) {
			return 0L;
		}
		@Override
		public long countPendingPublicByUserId(Long userId) {
			return 0L;
		}
		public List<Crew> findPublicCrews() {
			return crews.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}
	}

	protected static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
		@Override
		public java.util.List<com.bangpot.crew.domain.CrewMember> findAllByUserId(Long userId) {
			return java.util.List.of();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId);
		}

		private final Map<Long, CrewMember> members = new HashMap<>();
		private long sequence = 1L;

		@Override
		public CrewMember save(CrewMember crewMember) {
			if (crewMember.getId() == null) {
				crewMember.assignId(sequence++);
			}
			members.put(crewMember.getId(), crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return members.values().stream().anyMatch(member ->
				member.getCrewId().equals(crewId)
					&& member.getUserId().equals(userId)
					&& member.getStatus() == com.bangpot.crew.domain.CrewMemberStatus.ACTIVE
			);
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return members.values().stream().anyMatch(member ->
				member.getCrewId().equals(crewId)
					&& member.getUserId().equals(userId)
					&& member.getRole() == com.bangpot.crew.domain.CrewRole.LEADER
					&& member.getStatus() == com.bangpot.crew.domain.CrewMemberStatus.ACTIVE
			);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return members.values().stream()
				.filter(member -> member.getCrewId().equals(crewId)
					&& member.getUserId().equals(userId)
					&& member.getStatus() == com.bangpot.crew.domain.CrewMemberStatus.ACTIVE)
				.findFirst();
		}
		@Override
		public boolean existsActiveByCrewIdAndUserIdNot(Long crewId, Long userId) {
			return findAllByCrewId(crewId).stream()
				.filter(CrewMember::isActive)
				.anyMatch(member -> !userId.equals(member.getUserId()));
		}


		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return members.values().stream()
				.filter(member -> member.getCrewId().equals(crewId))
				.toList();
		}
	}

	protected static final class InMemoryMeetingRepository implements MeetingRepository {
		@Override
		public boolean existsUnfinishedByCrewId(Long crewId) {
			return false;
		}

		@Override
		public boolean existsUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId) {
			return false;
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return com.bangpot.meeting.domain.view.MyCalendarView.of(java.util.List.of(), 0);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return com.bangpot.meeting.domain.view.MyJoinedMeetingsView.of(
				java.util.List.of(),
				com.bangpot.meeting.domain.view.MyJoinedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return com.bangpot.meeting.domain.view.MyCreatedMeetingsView.of(
				java.util.List.of(),
				com.bangpot.meeting.domain.view.MyCreatedMeetingsView.Page.of(page, size, false)
			);
		}

		private final Map<Long, Meeting> meetings = new HashMap<>();
		private long sequence = 1L;

		@Override
		public Meeting save(Meeting meeting) {
			if (meeting.getId() == null) {
				meeting.assignId(sequence++);
			}
			meetings.put(meeting.getId(), meeting);
			return meeting;
		}

		@Override
		public List<Meeting> findAllByCrewId(Long crewId) {
			return meetings.values().stream().filter(meeting -> meeting.getCrewId().equals(crewId)).toList();
		}

		@Override
		public List<Meeting> findRecruitmentCloseTargets(java.time.LocalDateTime now, int limit) {
			return meetings.values().stream()
				.filter(meeting -> meeting.getStatus() == com.bangpot.meeting.domain.MeetingStatus.RECRUITING)
				.filter(meeting -> !startAt(meeting).isAfter(now))
				.sorted(Comparator
					.comparing(Meeting::getMeetingDate)
					.thenComparing(Meeting::getMeetingTime)
					.thenComparing(Meeting::getId))
				.limit(limit)
				.toList();
		}

		@Override
		public List<Meeting> findCompletionTargets(java.time.LocalDateTime completionCutoff, int limit) {
			return meetings.values().stream()
				.filter(meeting -> meeting.getStatus() == com.bangpot.meeting.domain.MeetingStatus.RECRUITMENT_CLOSED)
				.filter(meeting -> !startAt(meeting).isAfter(completionCutoff))
				.sorted(Comparator
					.comparing(Meeting::getMeetingDate)
					.thenComparing(Meeting::getMeetingTime)
					.thenComparing(Meeting::getId))
				.limit(limit)
				.toList();
		}

		private java.time.LocalDateTime startAt(Meeting meeting) {
			return java.time.LocalDate.parse(meeting.getMeetingDate()).atTime(java.time.LocalTime.parse(meeting.getMeetingTime()));
		}

		@Override
		public int cancelUnfinishedByCrewIdAndHostUserId(
			Long crewId,
			Long hostUserId,
			java.time.Instant updatedAt
		) {
			return 0;
		}

		@Override
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetings.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return findById(meetingId).filter(meeting -> meeting.getCrewId().equals(crewId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewIdForUpdate(Long meetingId, Long crewId) {
			return findByIdAndCrewId(meetingId, crewId);
		}

		@Override
		public int recordResultIfNotRecorded(
			Long meetingId,
			Long crewId,
			Long hostUserId,
			com.bangpot.meeting.domain.MeetingResult result,
			java.time.Instant updatedAt
		) {
			return 0;
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			return 0L;
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return 0L;
		}
	}

	protected static final class InMemoryMeetingParticipantRepository implements MeetingParticipantRepository {
		private final Map<Long, MeetingParticipant> participants = new HashMap<>();
		private long sequence = 1L;

		@Override
		public MeetingParticipant save(MeetingParticipant participant) {
			if (participant.getId() == null) {
				participant.assignId(sequence++);
			}
			participants.put(participant.getId(), participant);
			return participant;
		}

		@Override
		public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
			return participants.values().stream()
				.filter(participant -> participant.getMeetingId().equals(meetingId) && participant.getUserId().equals(userId))
				.findFirst();
		}

		@Override
		public int leaveInactiveCrewMemberParticipations(
			Long crewId,
			Long userId,
			java.time.Instant updatedAt
		) {
			return 0;
		}

		@Override
		public long countByMeetingId(Long meetingId) {
			return participants.values().stream()
				.filter(participant -> participant.getMeetingId().equals(meetingId))
				.filter(participant -> participant.getStatus().representsJoined())
				.count();
		}

		@Override
		public void delete(MeetingParticipant participant) {
			participants.remove(participant.getId());
		}
	}

	protected static final class InMemoryMeetingLogRepository implements MeetingLogRepository {
		private final InMemoryMeetingRepository meetingRepository;
		private final Map<Long, MeetingLog> logs = new HashMap<>();
		private long sequence = 1L;
		private boolean findByIdForUpdateCalled;

		private InMemoryMeetingLogRepository(InMemoryMeetingRepository meetingRepository) {
			this.meetingRepository = meetingRepository;
		}

		@Override
		public MeetingLog save(MeetingLog log) {
			if (log.getId() == null) {
				log.assignId(sequence++);
			}
			logs.put(log.getId(), log);
			return log;
		}

		@Override
		public Optional<MeetingLog> findById(Long logId) {
			return Optional.ofNullable(logs.get(logId))
				.filter(log -> log.getDeletedAt() == null);
		}

		@Override
		public Optional<MeetingLog> findByIdForUpdate(Long logId) {
			findByIdForUpdateCalled = true;
			return findById(logId);
		}

		@Override
		public Optional<MeetingLog> findActiveLogInCrewForUpdate(Long crewId, Long logId) {
			findByIdForUpdateCalled = true;
			return findById(logId)
				.filter(log -> meetingRepository.findById(log.getMeetingId())
					.map(meeting -> meeting.getCrewId().equals(crewId))
					.orElse(false));
		}

		void resetLockTracking() {
			findByIdForUpdateCalled = false;
		}

		boolean findByIdForUpdateCalled() {
			return findByIdForUpdateCalled;
		}

		Optional<MeetingLog> findAnyById(Long logId) {
			return Optional.ofNullable(logs.get(logId));
		}

		@Override
		public Optional<MeetingLog> findByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return logs.values().stream()
				.filter(log -> log.getMeetingId().equals(meetingId)
					&& log.getAuthorUserId().equals(authorUserId)
					&& log.getDeletedAt() == null)
				.findFirst();
		}

		@Override
		public boolean existsAnyByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return logs.values().stream()
				.anyMatch(log -> log.getMeetingId().equals(meetingId) && log.getAuthorUserId().equals(authorUserId));
		}

		@Override
		public boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return logs.values().stream()
				.anyMatch(log -> log.getMeetingId().equals(meetingId)
					&& log.getAuthorUserId().equals(authorUserId)
					&& log.getDeletedAt() != null);
		}
	}

	protected static final class InMemoryMeetingLogPhotoRepository implements MeetingLogPhotoRepository {
		private final Map<Long, MeetingLogPhoto> photos = new HashMap<>();
		private long sequence = 1L;

		@Override
		public List<MeetingLogPhoto> saveAll(List<MeetingLogPhoto> photosToSave) {
			List<MeetingLogPhoto> saved = new ArrayList<>();
			for (MeetingLogPhoto photo : photosToSave) {
				if (photo.getId() == null) {
					photo.assignId(sequence++);
				}
				photos.put(photo.getId(), photo);
				saved.add(photo);
			}
			return saved;
		}

		@Override
		public List<MeetingLogPhoto> findAllByLogId(Long logId) {
			return photos.values().stream()
				.filter(photo -> photo.getLogId().equals(logId))
				.sorted(Comparator.comparing(MeetingLogPhoto::getId))
				.toList();
		}

		@Override
		public void deleteByLogId(Long logId) {
			photos.entrySet().removeIf(entry -> entry.getValue().getLogId().equals(logId));
		}
	}

	protected static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {

		private final InMemoryCrewRepository crewRepository;
		private final InMemoryCrewMemberRepository crewMemberRepository;

		private InMemoryMeetingQueryRepository(
			InMemoryCrewRepository crewRepository,
			InMemoryCrewMemberRepository crewMemberRepository
		) {
			this.crewRepository = crewRepository;
			this.crewMemberRepository = crewMemberRepository;
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return com.bangpot.meeting.domain.view.MyCalendarView.of(List.of(), 0);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(
			Long userId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.MyCreatedMeetingsView.of(
				List.of(),
				com.bangpot.meeting.domain.view.MyCreatedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.bangpot.meeting.domain.view.MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(
			Long userId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.MyJoinedMeetingsView.of(
				List.of(),
				com.bangpot.meeting.domain.view.MyJoinedMeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.bangpot.meeting.domain.view.UpcomingMeetingsView findUpcomingMeetingsViewByUserId(
			Long userId,
			int limit,
			String currentDate,
			String currentTime
		) {
			return com.bangpot.meeting.domain.view.UpcomingMeetingsView.of(List.of(), 0L);
		}

		@Override
		public com.bangpot.meeting.domain.view.MeetingActivityRecordView findActivityRecordViewByUserId(Long userId) {
			return com.bangpot.meeting.domain.view.MeetingActivityRecordView.empty();
		}

		@Override
		public com.bangpot.meeting.domain.view.CrewScheduleView findCrewScheduleViewByCrewId(
			Long crewId,
			java.time.LocalDate from,
			java.time.LocalDate to
		) {
			return com.bangpot.meeting.domain.view.CrewScheduleView.of(List.of());
		}

		@Override
		public com.bangpot.meeting.domain.view.CrewMeetingGalleryView findCrewMeetingGalleryView(
			Long crewId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.CrewMeetingGalleryView.of(
				List.of(),
				com.bangpot.meeting.domain.view.CrewMeetingGalleryView.Page.of(page, size, false)
			);
		}

		@Override
		public Optional<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView>
			findCrewMeetingGalleryDetailTargetView(Long crewId, Long meetingId) {
			return Optional.empty();
		}

		@Override
		public List<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView.Photo>
			findCrewMeetingGalleryDetailPhotos(Long meetingId) {
			return List.of();
		}

		@Override
		public Optional<MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(Long crewId, Long userId) {
			return crewRepository.findById(crewId)
				.map(crew -> MeetingsAccessView.of(
					crew.getId(),
					crewMemberRepository.findByCrewIdAndUserId(crew.getId(), userId)
						.map(CrewMember::getRole)
						.orElse(null)
				));
		}

		@Override
		public com.bangpot.meeting.domain.view.MeetingsView findMeetingsViewByCrewId(
			Long crewId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.MeetingsView.of(
				List.of(),
				com.bangpot.meeting.domain.view.MeetingsView.Page.of(page, size, false)
			);
		}

		@Override
		public Optional<com.bangpot.meeting.domain.view.MeetingDetailView> findMeetingDetailView(
			Long crewId,
			Long meetingId,
			Long userId
		) {
			return Optional.empty();
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			return 0L;
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return 0L;
		}

		@Override
		public Map<Long, Integer> countCompletedByUserIds(Collection<Long> userIds) {
			return Map.of();
		}
	}

	protected static final class InMemoryMeetingLogQueryRepository implements MeetingLogQueryRepository {

		private final InMemoryMeetingRepository meetingRepository;
		private final InMemoryMeetingLogRepository meetingLogRepository;
		private final InMemoryMeetingLogPhotoRepository meetingLogPhotoRepository;
		private final InMemoryUserRepository userRepository;

		private InMemoryMeetingLogQueryRepository(
			InMemoryMeetingRepository meetingRepository,
			InMemoryMeetingLogRepository meetingLogRepository,
			InMemoryMeetingLogPhotoRepository meetingLogPhotoRepository,
			InMemoryUserRepository userRepository
		) {
			this.meetingRepository = meetingRepository;
			this.meetingLogRepository = meetingLogRepository;
			this.meetingLogPhotoRepository = meetingLogPhotoRepository;
			this.userRepository = userRepository;
		}

		@Override
		public com.bangpot.meeting.domain.view.MyMeetingLogsView findMyMeetingLogsViewByAuthorUserId(
			Long userId,
			int page,
			int size
		) {
			return com.bangpot.meeting.domain.view.MyMeetingLogsView.of(
				List.of(),
				com.bangpot.meeting.domain.view.MyMeetingLogsView.Page.of(page, size, false)
			);
		}

		@Override
		public boolean existsMeetingById(Long meetingId) {
			return meetingRepository.findById(meetingId).isPresent();
		}

		@Override
		public Optional<MyMeetingLogView> findMyMeetingLogView(
			Long meetingId,
			Long authorUserId
		) {
			return meetingLogRepository.findByMeetingIdAndAuthorUserId(meetingId, authorUserId)
				.flatMap(log -> meetingRepository.findById(meetingId)
					.flatMap(meeting -> userRepository.findById(authorUserId)
						.map(user -> MyMeetingLogView.of(
							log.getId(),
							meeting.getId(),
							meeting.getTitle(),
							meeting.getThemeName(),
							meeting.getPlace(),
							meeting.getMeetingDate(),
							user.getNickname(),
							log.getCreatedAt(),
							log.getUpdatedAt(),
							log.getBody(),
							meetingLogPhotoRepository.findAllByLogId(log.getId()).stream()
								.map(MeetingLogPhoto::getPhotoUrl)
								.toList()
						))));
		}

		@Override
		public Optional<MeetingLogDetailView> findMeetingLogDetailView(Long crewId, Long logId) {
			return meetingLogRepository.findById(logId)
				.flatMap(log -> meetingRepository.findByIdAndCrewId(log.getMeetingId(), crewId)
					.flatMap(meeting -> userRepository.findById(log.getAuthorUserId())
						.map(user -> MeetingLogDetailView.of(
							log.getId(),
							meeting.getId(),
							meeting.getTitle(),
							meeting.getThemeName(),
							meeting.getPlace(),
							meeting.getMeetingDate(),
							user.getNickname(),
							log.getCreatedAt(),
							log.getUpdatedAt(),
							log.getBody(),
							meetingLogPhotoRepository.findAllByLogId(log.getId()).stream()
								.map(MeetingLogPhoto::getPhotoUrl)
								.toList()
						))));
		}

		@Override
		public boolean existsDeletedByMeetingIdAndAuthorUserId(Long meetingId, Long authorUserId) {
			return meetingLogRepository.existsDeletedByMeetingIdAndAuthorUserId(meetingId, authorUserId);
		}
	}
}


