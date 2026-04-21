package com.bangpot.meeting.application;

import java.time.Instant;
import java.util.ArrayList;
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
import com.bangpot.meeting.application.port.MeetingLogPhotoRepository;
import com.bangpot.meeting.application.port.MeetingLogRepository;
import com.bangpot.meeting.application.port.MeetingParticipantRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.application.service.CreateMeetingLogService;
import com.bangpot.meeting.application.service.DeleteMeetingLogService;
import com.bangpot.meeting.application.service.GetMeetingLogDetailService;
import com.bangpot.meeting.application.service.GetMyMeetingLogService;
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
		meetingLogRepository = new InMemoryMeetingLogRepository();
		meetingLogPhotoRepository = new InMemoryMeetingLogPhotoRepository();
		createMeetingLogUseCase = new CreateMeetingLogService(
			completedUserAccessService,
			meetingRepository,
			meetingParticipantRepository,
			meetingLogRepository,
			meetingLogPhotoRepository,
			userRepository
		);
		updateMeetingLogUseCase = new UpdateMeetingLogService(
			completedUserAccessService,
			meetingLogRepository,
			meetingLogPhotoRepository
		);
		deleteMeetingLogUseCase = new DeleteMeetingLogService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingRepository,
			meetingLogRepository
		);
		getMyMeetingLogUseCase = new GetMyMeetingLogService(
			completedUserAccessService,
			meetingRepository,
			meetingLogRepository,
			meetingLogPhotoRepository,
			userRepository
		);
		getMeetingLogDetailUseCase = new GetMeetingLogDetailService(
			completedUserAccessService,
			crewRepository,
			crewMemberRepository,
			meetingLogRepository,
			meetingLogPhotoRepository,
			meetingRepository,
			userRepository
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
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}
	}

	protected static final class InMemoryCrewRepository implements CrewRepository {
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
		public Optional<Crew> findAnyById(Long crewId) {
			return Optional.ofNullable(crews.get(crewId));
		}

				@Override
		public long countActiveByMemberUserId(Long userId) {
			return 0L;
		}
		@Override
		public long countPendingPublicByUserId(Long userId) {
			return 0L;
		}@Override
		public List<Crew> findPublicCrews() {
			return crews.values().stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}
	}

	protected static final class InMemoryCrewMemberRepository implements CrewMemberRepository {
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
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return members.values().stream()
				.filter(member -> member.getCrewId().equals(crewId))
				.toList();
		}
	}

	protected static final class InMemoryMeetingRepository implements MeetingRepository {
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
		public Optional<Meeting> findById(Long meetingId) {
			return Optional.ofNullable(meetings.get(meetingId));
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
			return findById(meetingId).filter(meeting -> meeting.getCrewId().equals(crewId));
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
		private final Map<Long, MeetingLog> logs = new HashMap<>();
		private long sequence = 1L;

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
}
