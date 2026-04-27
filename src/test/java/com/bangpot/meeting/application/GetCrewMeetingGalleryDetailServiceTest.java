package com.bangpot.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.meeting.application.exception.MeetingGalleryNotFoundException;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.service.GetCrewMeetingGalleryDetailService;
import com.bangpot.meeting.application.usecase.GetCrewMeetingGalleryDetailUseCase;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView;
import com.bangpot.meeting.domain.view.CrewMeetingGalleryView;
import com.bangpot.meeting.domain.view.CrewScheduleView;
import com.bangpot.meeting.domain.view.MeetingDetailView;
import com.bangpot.meeting.domain.view.MeetingsAccessView;
import com.bangpot.meeting.domain.view.MeetingsView;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class GetCrewMeetingGalleryDetailServiceTest {

	private InMemoryUserRepository userRepository;
	private InMemoryMeetingQueryRepository meetingQueryRepository;
	private GetCrewMeetingGalleryDetailUseCase getCrewMeetingGalleryDetailUseCase;

	@BeforeEach
	void setUp() {
		userRepository = new InMemoryUserRepository();
		meetingQueryRepository = new InMemoryMeetingQueryRepository();
		getCrewMeetingGalleryDetailUseCase = new GetCrewMeetingGalleryDetailService(
			new CompletedUserAccessService(userRepository),
			meetingQueryRepository
		);
	}

	@Test
	void returnsMeetingGalleryDetailForActiveCrewMember() {
		userRepository.save(User.create(7L, "member"));
		meetingQueryRepository.accessView = Optional.of(MeetingsAccessView.of(1L, CrewRole.MEMBER));
		meetingQueryRepository.targetView = Optional.of(
			CrewMeetingGalleryDetailTargetView.of(31L, "2026-04-12", "금요일 이스케이프")
		);
		meetingQueryRepository.photos = List.of(
			CrewMeetingGalleryDetailView.Photo.of(501L, "https://cdn.example.com/a.jpg", 1),
			CrewMeetingGalleryDetailView.Photo.of(502L, "https://cdn.example.com/b.jpg", 2)
		);

		CrewMeetingGalleryDetailView result = getCrewMeetingGalleryDetailUseCase.handle(
			GetCrewMeetingGalleryDetailUseCase.Query.of(1L, 31L, 7L)
		);

		assertThat(result.meetingId()).isEqualTo(31L);
		assertThat(result.meetingDate()).isEqualTo("2026-04-12");
		assertThat(result.meetingTitle()).isEqualTo("금요일 이스케이프");
		assertThat(result.photos()).extracting(CrewMeetingGalleryDetailView.Photo::photoId)
			.containsExactly(501L, 502L);
		assertThat(result.photos()).extracting(CrewMeetingGalleryDetailView.Photo::order)
			.containsExactly(1, 2);
		assertThat(result.totalPhotoCount()).isEqualTo(2);
	}

	@Test
	void throwsMeetingGalleryNotFoundWhenMeetingIsNotGalleryTarget() {
		userRepository.save(User.create(7L, "member"));
		meetingQueryRepository.accessView = Optional.of(MeetingsAccessView.of(1L, CrewRole.MEMBER));
		meetingQueryRepository.targetView = Optional.empty();

		assertThatThrownBy(() -> getCrewMeetingGalleryDetailUseCase.handle(
			GetCrewMeetingGalleryDetailUseCase.Query.of(1L, 999L, 7L)
		))
			.isInstanceOf(MeetingGalleryNotFoundException.class);
	}

	@Test
	void throwsCrewNotFoundWhenCrewDoesNotExist() {
		userRepository.save(User.create(7L, "member"));
		meetingQueryRepository.accessView = Optional.empty();

		assertThatThrownBy(() -> getCrewMeetingGalleryDetailUseCase.handle(
			GetCrewMeetingGalleryDetailUseCase.Query.of(999L, 31L, 7L)
		))
			.isInstanceOf(CrewNotFoundException.class);
	}

	@Test
	void throwsAccessDeniedWhenUserIsNotActiveCrewMember() {
		userRepository.save(User.create(7L, "member"));
		meetingQueryRepository.accessView = Optional.of(MeetingsAccessView.of(1L, null));

		assertThatThrownBy(() -> getCrewMeetingGalleryDetailUseCase.handle(
			GetCrewMeetingGalleryDetailUseCase.Query.of(1L, 31L, 7L)
		))
			.isInstanceOf(AccessDeniedException.class);
	}

	private static final class InMemoryUserRepository implements UserRepository {

		private final java.util.Map<Long, User> users = new java.util.HashMap<>();

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
		public List<User> findAllCompletedUsers() {
			return users.values().stream().toList();
		}

		@Override
		public User save(User user) {
			users.put(user.getId(), user);
			return user;
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			return false;
		}

		@Override
		public void withdrawById(Long userId, String anonymizedNickname, Instant withdrawnAt) {
		}
	}

	private static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {

		private Optional<MeetingsAccessView> accessView = Optional.empty();
		private Optional<CrewMeetingGalleryDetailTargetView> targetView = Optional.empty();
		private List<CrewMeetingGalleryDetailView.Photo> photos = List.of();

		@Override
		public Optional<MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(Long crewId, Long userId) {
			return accessView;
		}

		@Override
		public Optional<CrewMeetingGalleryDetailTargetView> findCrewMeetingGalleryDetailTargetView(
			Long crewId,
			Long meetingId
		) {
			return targetView;
		}

		@Override
		public List<CrewMeetingGalleryDetailView.Photo> findCrewMeetingGalleryDetailPhotos(Long meetingId) {
			return photos;
		}

		@Override
		public MyCalendarView findMyCalendarViewByUserId(Long userId) {
			return MyCalendarView.of(List.of(), 0);
		}

		@Override
		public MyCreatedMeetingsView findMyCreatedMeetingsViewByHostUserId(Long userId, int page, int size) {
			return MyCreatedMeetingsView.of(List.of(), MyCreatedMeetingsView.Page.of(page, size, false));
		}

		@Override
		public MyJoinedMeetingsView findMyJoinedMeetingsViewByUserId(Long userId, int page, int size) {
			return MyJoinedMeetingsView.of(List.of(), MyJoinedMeetingsView.Page.of(page, size, false));
		}

		@Override
		public UpcomingMeetingsView findUpcomingMeetingsViewByUserId(
			Long userId,
			int limit,
			String currentDate,
			String currentTime
		) {
			return UpcomingMeetingsView.of(List.of(), 0L);
		}

		@Override
		public CrewScheduleView findCrewScheduleViewByCrewId(Long crewId, LocalDate from, LocalDate to) {
			return CrewScheduleView.of(List.of());
		}

		@Override
		public CrewMeetingGalleryView findCrewMeetingGalleryView(Long crewId, int page, int size) {
			return CrewMeetingGalleryView.of(List.of(), CrewMeetingGalleryView.Page.of(page, size, false));
		}

		@Override
		public MeetingsView findMeetingsViewByCrewId(Long crewId, int page, int size) {
			return MeetingsView.of(List.of(), MeetingsView.Page.of(page, size, false));
		}

		@Override
		public Optional<MeetingDetailView> findMeetingDetailView(Long crewId, Long meetingId, Long userId) {
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
}
