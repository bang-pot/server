package com.bangpot.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.application.service.CompleteTempUserService;
import com.bangpot.auth.application.service.GetCurrentAuthUserService;
import com.bangpot.auth.application.service.LoginWithProviderService;
import com.bangpot.auth.application.usecase.CompleteTempUserUseCase;
import com.bangpot.auth.application.usecase.GetCurrentAuthUserUseCase;
import com.bangpot.auth.application.usecase.LoginWithProviderUseCase;
import com.bangpot.auth.domain.AuthProvider;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.auth.domain.AuthUserStatus;
import com.bangpot.auth.domain.RequiredTermsAgreement;
import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;
import com.bangpot.auth.application.config.AuthRequiredTermsProperties;
import com.bangpot.crew.application.port.CrewQueryRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.meeting.application.port.MeetingQueryRepository;
import com.bangpot.meeting.application.port.MeetingRepository;
import com.bangpot.meeting.domain.Meeting;
import com.bangpot.meeting.domain.view.MyCalendarView;
import com.bangpot.meeting.domain.view.MyCreatedMeetingsView;
import com.bangpot.meeting.domain.view.MyJoinedMeetingsView;
import com.bangpot.meeting.domain.view.UpcomingMeetingsView;
import com.bangpot.crew.domain.view.PublicCrewPreviewView;
import com.bangpot.user.application.exception.DuplicateNicknameException;
import com.bangpot.user.application.exception.InvalidNicknameException;
import com.bangpot.user.application.port.UserQueryRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CheckNicknameAvailabilityService;
import com.bangpot.user.application.service.GetMyProfileService;
import com.bangpot.user.application.service.UpdateMyProfileService;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.domain.User;
import com.bangpot.user.domain.view.MyProfileView;
import com.bangpot.user.domain.view.UserProfileView;

class AuthUseCaseServicesTest {

	private static final Instant NOW = Instant.parse("2026-03-31T00:00:00Z");

	private InMemoryAuthUserRepository authUserRepository;
	private InMemoryUserRepository userRepository;
	private InMemoryUserQueryRepository userQueryRepository;
	private InMemoryMeetingRepository meetingRepository;
	private InMemoryCrewRepository crewRepository;
	private InMemoryMeetingQueryRepository meetingQueryRepository;
	private InMemoryCrewQueryRepository crewQueryRepository;
	private LoginWithProviderUseCase loginWithProviderUseCase;
	private CompleteTempUserUseCase completeTempUserUseCase;
	private CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private GetCurrentAuthUserUseCase getCurrentAuthUserUseCase;
	private GetMyProfileUseCase getMyProfileUseCase;
	private UpdateMyProfileUseCase updateMyProfileUseCase;
	private AuthAuditLogger authAuditLogger;

	@BeforeEach
	void setUp() {
		authUserRepository = new InMemoryAuthUserRepository();
		userRepository = new InMemoryUserRepository();
		meetingRepository = new InMemoryMeetingRepository();
		crewRepository = new InMemoryCrewRepository();
		userQueryRepository = new InMemoryUserQueryRepository(userRepository);
		meetingQueryRepository = new InMemoryMeetingQueryRepository(meetingRepository);
		crewQueryRepository = new InMemoryCrewQueryRepository(crewRepository);
		Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
		authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		AuthRequiredTermsProperties authRequiredTermsProperties = new AuthRequiredTermsProperties();
		authRequiredTermsProperties.setRequiredTermsVersion("2026-03-25");

		loginWithProviderUseCase = new LoginWithProviderService(authUserRepository, authAuditLogger);
		completeTempUserUseCase = new CompleteTempUserService(
			authUserRepository,
			userRepository,
			clock,
			authRequiredTermsProperties,
			authAuditLogger
		);
		checkNicknameAvailabilityUseCase = new CheckNicknameAvailabilityService(userRepository);
		getCurrentAuthUserUseCase = new GetCurrentAuthUserService(
			authUserRepository,
			userRepository,
			authRequiredTermsProperties
		);
		getMyProfileUseCase = new GetMyProfileService(userQueryRepository, meetingQueryRepository, crewQueryRepository);
		updateMyProfileUseCase = new UpdateMyProfileService(userRepository);
	}

	@Test
	void logsInExistingFullUserByProviderId() {
		AuthUser existingUser = AuthUser.rehydrate(
			1L,
			AuthProvider.KAKAO,
			"1001",
			AuthUserStatus.FULL,
			"bangpot",
			RequiredTermsAgreement.of("2026-03-01", NOW.minusSeconds(3600)),
			null,
			NOW.minusSeconds(7200),
			NOW.minusSeconds(3600)
		);
		authUserRepository.save(existingUser);
		userRepository.save(User.create(existingUser.getId(), "bangpot"));

		LoginWithProviderUseCase.Result result = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "1001", "/protected-demo")
		);

		assertThat(result.userId()).isEqualTo(existingUser.getId());
		assertThat(result.authStatus()).isEqualTo(AuthUserStatus.FULL);
		assertThat(result.completionRequired()).isFalse();
		assertThat(result.nextPath()).isEqualTo("/protected-demo");
		verify(authAuditLogger).loginSucceeded(
			AuthProvider.KAKAO,
			existingUser.getId(),
			AuthUserStatus.FULL,
			false
		);
	}

	@Test
	void createsTempUserForNewProviderAndKeepsCompletionRequiredOnRelogin() {
		LoginWithProviderUseCase.Result firstLogin = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "2002", "/protected-demo")
		);
		LoginWithProviderUseCase.Result secondLogin = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "2002", null)
		);

		assertThat(firstLogin.authStatus()).isEqualTo(AuthUserStatus.TEMP);
		assertThat(firstLogin.completionRequired()).isTrue();
		assertThat(firstLogin.nextPath()).isEqualTo("/auth/complete");
		assertThat(firstLogin.pendingRedirectPath()).isEqualTo("/protected-demo");

		assertThat(secondLogin.userId()).isEqualTo(firstLogin.userId());
		assertThat(secondLogin.authStatus()).isEqualTo(AuthUserStatus.TEMP);
		assertThat(secondLogin.completionRequired()).isTrue();
		assertThat(secondLogin.nextPath()).isEqualTo("/auth/complete");
		assertThat(secondLogin.pendingRedirectPath()).isEqualTo("/protected-demo");
		verify(authAuditLogger, times(2)).loginSucceeded(
			AuthProvider.KAKAO,
			firstLogin.userId(),
			AuthUserStatus.TEMP,
			true
		);
	}

	@Test
	void completesTempUserProfileAndPromotesUserToFull() {
		LoginWithProviderUseCase.Result loginResult = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "3003", "/protected-demo")
		);

		CompleteTempUserUseCase.Result completionResult = completeTempUserUseCase.handle(
			CompleteTempUserUseCase.Command.of(loginResult.userId(), "potmaster", true)
		);

		assertThat(completionResult.authStatus()).isEqualTo(AuthUserStatus.FULL);
		assertThat(completionResult.completionRequired()).isFalse();
		assertThat(completionResult.nextPath()).isEqualTo("/protected-demo");
		assertThat(userRepository.findById(loginResult.userId())).isPresent()
			.get()
			.extracting(User::getNickname)
			.isEqualTo("potmaster");
		assertThat(authUserRepository.findById(loginResult.userId())).isPresent()
			.get()
			.extracting(AuthUser::getNickname)
			.isNull();

		GetCurrentAuthUserUseCase.View meView = getCurrentAuthUserUseCase.handle(
			GetCurrentAuthUserUseCase.Query.of(loginResult.userId())
		);
		assertThat(meView.authStatus()).isEqualTo(GetCurrentAuthUserUseCase.AuthStatus.FULL);
		assertThat(meView.user()).isNotNull();
		assertThat(meView.user().nickname()).isEqualTo("potmaster");
		assertThat(meView.requiredTermsVersion()).isEqualTo("2026-03-25");
		verify(authAuditLogger).authStateChanged(
			loginResult.userId(),
			AuthUserStatus.TEMP,
			AuthUserStatus.FULL,
			"profile_completed"
		);
	}

	@Test
	void rejectsDuplicateNicknameAtCompletionTime() {
		authUserRepository.save(AuthUser.rehydrate(
			10L,
			AuthProvider.KAKAO,
			"existing",
			AuthUserStatus.FULL,
			"dupename",
			RequiredTermsAgreement.of("2026-03-01", NOW.minusSeconds(10)),
			null,
			NOW.minusSeconds(100),
			NOW.minusSeconds(10)
		));
		userRepository.save(User.create(10L, "dupename"));
		LoginWithProviderUseCase.Result loginResult = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "4004", null)
		);

		assertThat(checkNicknameAvailabilityUseCase.handle(
			CheckNicknameAvailabilityUseCase.Query.of("dupename")
		).available()).isFalse();
		assertThatThrownBy(() -> completeTempUserUseCase.handle(
			CompleteTempUserUseCase.Command.of(loginResult.userId(), "dupename", true)
		))
			.isInstanceOf(DuplicateNicknameException.class);
	}

	@Test
	void rejectsInvalidNicknameAtCompletionTime() {
		LoginWithProviderUseCase.Result loginResult = loginWithProviderUseCase.handle(
			LoginWithProviderUseCase.Command.of(AuthProvider.KAKAO, "5005", null)
		);

		assertThatThrownBy(() -> completeTempUserUseCase.handle(
			CompleteTempUserUseCase.Command.of(loginResult.userId(), "pot-master", true)
		))
			.isInstanceOf(InvalidNicknameException.class);
	}

	@Test
	void returnsCurrentProfileForFullUser() {
		AuthUser fullUser = AuthUser.rehydrate(
			1L,
			AuthProvider.KAKAO,
			"full-user",
			AuthUserStatus.FULL,
			"bangpot",
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
		authUserRepository.save(fullUser);
		userRepository.save(User.create(fullUser.getId(), "bangpot"));

		MyProfileView result = getMyProfileUseCase.handle(GetMyProfileUseCase.Query.of(fullUser.getId()));

		assertThat(result.id()).isEqualTo(fullUser.getId());
		assertThat(result.nickname()).isEqualTo("bangpot");
	}

	@Test
	void updatesNicknameForCurrentFullUser() {
		AuthUser fullUser = AuthUser.rehydrate(
			1L,
			AuthProvider.KAKAO,
			"full-user",
			AuthUserStatus.FULL,
			"before",
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(60)),
			null,
			NOW.minusSeconds(3600),
			NOW.minusSeconds(60)
		);
		authUserRepository.save(fullUser);
		userRepository.save(User.create(fullUser.getId(), "before"));

		updateMyProfileUseCase.handle(UpdateMyProfileUseCase.Command.of(fullUser.getId(), "  after  "));
		assertThat(userRepository.findById(fullUser.getId())).get()
			.extracting(User::getNickname)
			.isEqualTo("after");
	}

	@Test
	void rejectsDuplicateNicknameWhenUpdatingProfile() {
		authUserRepository.save(AuthUser.rehydrate(
			10L,
			AuthProvider.KAKAO,
			"existing",
			AuthUserStatus.FULL,
			"dupe-name",
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(10)),
			null,
			NOW.minusSeconds(100),
			NOW.minusSeconds(10)
		));
		userRepository.save(User.create(10L, "dupename"));
		AuthUser fullUser = AuthUser.rehydrate(
			11L,
			AuthProvider.KAKAO,
			"updating-user",
			AuthUserStatus.FULL,
			"before",
			RequiredTermsAgreement.of("2026-03-25", NOW.minusSeconds(10)),
			null,
			NOW.minusSeconds(100),
			NOW.minusSeconds(10)
		);
		authUserRepository.save(fullUser);
		userRepository.save(User.create(fullUser.getId(), "before"));

		assertThatThrownBy(() -> updateMyProfileUseCase.handle(
			UpdateMyProfileUseCase.Command.of(fullUser.getId(), "dupename")
		))
			.isInstanceOf(DuplicateNicknameException.class);
	}

	private static final class InMemoryAuthUserRepository implements AuthUserRepository {
		@Override
		public void deleteById(Long userId) {
		}


		private final Map<Long, AuthUser> usersById = new HashMap<>();
		private final Map<String, Long> idsByProviderId = new HashMap<>();
		private long sequence = 1L;

		@Override
		public Optional<AuthUser> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		@Override
		public Optional<AuthUser> findByProviderAndProviderId(AuthProvider provider, String providerId) {
			Long userId = idsByProviderId.get(provider.name() + ":" + providerId);
			return userId == null ? Optional.empty() : findById(userId);
		}

		public boolean existsByNickname(String nickname) {
			return usersById.values().stream()
				.anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public AuthUser save(AuthUser user) {
			if (user.getId() == null) {
				user.assignId(sequence++);
			}
			usersById.put(user.getId(), user);
			idsByProviderId.put(user.getProvider().name() + ":" + user.getProviderId(), user.getId());
			return user;
		}
	}

	private static final class InMemoryUserRepository implements UserRepository {
		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		}


		private final Map<Long, User> usersById = new HashMap<>();

		@Override
		public Optional<User> findById(Long userId) {
			return Optional.ofNullable(usersById.get(userId));
		}

		public boolean existsByNickname(String nickname) {
			return usersById.values().stream()
				.anyMatch(user -> nickname.equals(user.getNickname()));
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			String normalizedKeyword = nickname == null ? null : nickname.trim().toLowerCase();
			if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
				normalizedKeyword = null;
			}
			final String keyword = normalizedKeyword;
			return usersById.values().stream()
				.filter(user -> keyword == null || (user.getNickname() != null && user.getNickname().toLowerCase().contains(keyword)))
				.sorted((left, right) -> Long.compare(left.getId(), right.getId()))
				.toList();
		}

		@Override
		public java.util.List<com.bangpot.user.domain.User> findAllCompletedUsers() {
			return findCompletedUsersByNicknameContaining(null);
		}

		@Override
		public User save(User user) {
			usersById.put(user.getId(), user);
			return user;
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			User user = usersById.get(userId);
			if (user == null) {
				return false;
			}
			user.updateNickname(nickname);
			return true;
		}
	}

	private static final class InMemoryUserQueryRepository implements UserQueryRepository {

		private final InMemoryUserRepository userRepository;

		private InMemoryUserQueryRepository(InMemoryUserRepository userRepository) {
			this.userRepository = userRepository;
		}

		@Override
		public UserProfileView findMyProfileUserViewByUserId(Long userId) {
			return userRepository.findById(userId)
				.map(user -> UserProfileView.of(
					user.getId(),
					user.getNickname(),
					null
				))
				.orElse(null);
		}

		@Override
		public boolean existsCompletedUser(Long userId) {
			return userRepository.findById(userId).isPresent();
		}

		@Override
		public com.bangpot.user.domain.view.UserSearchView searchUsersByNickname(String nickname, int page, int size) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryMeetingQueryRepository implements MeetingQueryRepository {

		private final InMemoryMeetingRepository meetingRepository;

		private InMemoryMeetingQueryRepository(InMemoryMeetingRepository meetingRepository) {
			this.meetingRepository = meetingRepository;
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
		public UpcomingMeetingsView findUpcomingMeetingsViewByUserId(Long userId, int limit, String currentDate, String currentTime) {
			return UpcomingMeetingsView.of(List.of(), 0L);
		}

		@Override
		public com.bangpot.meeting.domain.view.CrewScheduleView findCrewScheduleViewByCrewId(
			Long crewId,
			java.time.LocalDate from,
			java.time.LocalDate to
		) {
			throw new UnsupportedOperationException();
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
		public java.util.Optional<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailTargetView> findCrewMeetingGalleryDetailTargetView(
			Long crewId,
			Long meetingId
		) {
			return java.util.Optional.empty();
		}

		@Override
		public List<com.bangpot.meeting.domain.view.CrewMeetingGalleryDetailView.Photo> findCrewMeetingGalleryDetailPhotos(
			Long meetingId
		) {
			return List.of();
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.MeetingsAccessView> findMeetingsAccessViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.meeting.domain.view.MeetingsView findMeetingsViewByCrewId(Long crewId, int page, int size) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.meeting.domain.view.MeetingDetailView> findMeetingDetailView(
			Long crewId,
			Long meetingId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long countCreatedByHostUserId(Long userId) {
			return meetingRepository.countCreatedByHostUserId(userId);
		}

		@Override
		public long countJoinedByUserId(Long userId) {
			return meetingRepository.countJoinedByUserId(userId);
		}

		@Override
		public java.util.Map<Long, Integer> countCompletedByUserIds(java.util.Collection<Long> userIds) {
			return java.util.Map.of();
		}
	}

	private static final class InMemoryCrewQueryRepository implements CrewQueryRepository {

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewInviteCandidateAccessView>
			findCrewInviteCandidateAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.bangpot.crew.domain.view.CrewInviteCandidatesView findCrewInviteCandidatesView(
			Long crewId,
			Long leaderUserId,
			String nickname,
			int page,
			int size
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewJoinView> findCrewJoinViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewHubView> findCrewHubViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMemberAccessView>
			findCrewMemberAccessByCrewIdAndUserId(Long crewId, Long userId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewMembersView> findCrewMembersViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}


		@Override
		public java.util.Optional<com.bangpot.crew.domain.view.CrewPoliciesView> findCrewPoliciesViewByCrewIdAndUserId(
			Long crewId,
			Long userId
		) {
			throw new UnsupportedOperationException();
		}		@Override
		public java.util.List<com.bangpot.user.domain.view.MyWithdrawalCheckView.BlockingActiveCrew> findWithdrawalBlockingActiveCrewsByMemberUserId(Long userId) {
			return java.util.List.of();
		}

		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return com.bangpot.crew.domain.view.MyCrewsView.of(
				java.util.List.of(),
				com.bangpot.crew.domain.view.MyCrewsView.Page.of(page, size, false)
			);
		}

		@Override
		public long countMyCrewsViewByMemberUserId(Long userId) {
			return crewRepository.countActiveByMemberUserId(userId);
		}

		@Override
		public PublicCrewPreviewView findPublicCrewPreviewView(int limit) {
			return PublicCrewPreviewView.of(List.of());
		}

		@Override
		public com.bangpot.crew.domain.view.PublicCrewCardsView findPublicCrewCardsView(int page, int size) {
			return com.bangpot.crew.domain.view.PublicCrewCardsView.of(
				List.of(),
				com.bangpot.crew.domain.view.PublicCrewCardsView.Page.of(page, size, false)
			);
		}

		@Override
		public com.bangpot.crew.domain.view.MeetingCreateCrewsView findActiveCrewsByUserId(Long userId) {
			return com.bangpot.crew.domain.view.MeetingCreateCrewsView.of(List.of());
		}

		private final InMemoryCrewRepository crewRepository;

		private InMemoryCrewQueryRepository(InMemoryCrewRepository crewRepository) {
			this.crewRepository = crewRepository;
		}

		@Override
		public long countActiveByMemberUserId(Long userId) {
			return crewRepository.countActiveByMemberUserId(userId);
		}

		@Override
		public long countPendingPublicByUserId(Long userId) {
			return crewRepository.countPendingPublicByUserId(userId);
		}
	}

	private static final class InMemoryMeetingRepository implements MeetingRepository {
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

		@Override
		public boolean existsUnfinishedByCrewId(Long crewId) {
			return false;
		}

		@Override
		public boolean existsUnfinishedByCrewIdAndHostUserId(Long crewId, Long hostUserId) {
			return false;
		}


		@Override
		public Meeting save(Meeting meeting) {
			return meeting;
		}

		@Override
		public List<Meeting> findAllByCrewId(Long crewId) {
			return List.of();
		}

		@Override
		public List<Meeting> findRecruitmentCloseTargets(java.time.LocalDateTime now, int limit) {
			return List.of();
		}

		@Override
		public List<Meeting> findCompletionTargets(java.time.LocalDateTime completionCutoff, int limit) {
			return List.of();
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
			return Optional.empty();
		}

		@Override
		public Optional<Meeting> findByIdAndCrewId(Long meetingId, Long crewId) {
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
	}

	private static final class InMemoryCrewRepository implements CrewRepository {
		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return com.bangpot.crew.domain.view.MyCrewsView.of(
				java.util.List.of(),
				com.bangpot.crew.domain.view.MyCrewsView.Page.of(page, size, false)
			);
		}

		@Override
		public java.util.Optional<com.bangpot.crew.domain.Crew> findAnyById(Long crewId) {
			return findById(crewId);
		}


		@Override
		public boolean existsByName(String name) {
			return false;
		}

		@Override
		public Crew save(Crew crew) {
			return crew;
		}

		@Override
		public Optional<Crew> findById(Long crewId) {
			return Optional.empty();
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
			return List.of();
		}
	}
}


