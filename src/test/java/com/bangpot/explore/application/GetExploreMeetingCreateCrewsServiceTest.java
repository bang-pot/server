package com.bangpot.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.explore.application.service.GetExploreMeetingCreateCrewsService;
import com.bangpot.explore.application.usecase.GetExploreMeetingCreateCrewsUseCase;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

class GetExploreMeetingCreateCrewsServiceTest {

	private InMemoryCrewRepository crewRepository;
	private InMemoryCrewMemberRepository crewMemberRepository;
	private InMemoryUserRepository userRepository;
	private GetExploreMeetingCreateCrewsUseCase useCase;

	@BeforeEach
	void setUp() {
		crewRepository = new InMemoryCrewRepository();
		crewMemberRepository = new InMemoryCrewMemberRepository();
		userRepository = new InMemoryUserRepository();
		useCase = new GetExploreMeetingCreateCrewsService(
			new CompletedUserAccessService(userRepository),
			crewRepository,
			crewMemberRepository
		);
	}

	@Test
	void returnsActiveCrewsForCompletedUser() {
		userRepository.save(User.create(1L, "alpha"));
		crewRepository.save(activeCrew(100L, "Alpha Crew"));
		crewRepository.save(activeCrew(200L, "Beta Crew"));
		crewRepository.save(deletedCrew(300L, "Deleted Crew"));
		crewMemberRepository.save(CrewMember.createMember(200L, 1L));
		crewMemberRepository.save(CrewMember.createLeader(100L, 1L));
		CrewMember removedMembership = CrewMember.createMember(300L, 1L);
		removedMembership.remove();
		crewMemberRepository.save(removedMembership);

		GetExploreMeetingCreateCrewsUseCase.Result result = useCase.handle(
			GetExploreMeetingCreateCrewsUseCase.Query.of(1L)
		);

		assertThat(result.crews())
			.extracting(GetExploreMeetingCreateCrewsUseCase.CrewItem::crewName)
			.containsExactly("Alpha Crew", "Beta Crew");
	}

	@Test
	void returnsEmptyArrayWhenUserHasNoActiveCrews() {
		userRepository.save(User.create(1L, "alpha"));

		GetExploreMeetingCreateCrewsUseCase.Result result = useCase.handle(
			GetExploreMeetingCreateCrewsUseCase.Query.of(1L)
		);

		assertThat(result.crews()).isEmpty();
	}

	@Test
	void deniesIncompleteUser() {
		assertThatThrownBy(() -> useCase.handle(GetExploreMeetingCreateCrewsUseCase.Query.of(1L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	private Crew activeCrew(Long id, String name) {
		Crew crew = Crew.create(name, "desc", null, null);
		crew.assignId(id);
		return crew;
	}

	private Crew deletedCrew(Long id, String name) {
		Crew crew = activeCrew(id, name);
		crew.delete();
		return crew;
	}

	private static final class InMemoryCrewRepository implements CrewRepository {
		@Override
		public com.bangpot.crew.domain.view.MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
			return com.bangpot.crew.domain.view.MyCrewsView.of(
				java.util.List.of(),
				com.bangpot.crew.domain.view.MyCrewsView.Page.of(page, size, false)
			);
		}


		private final List<Crew> crews = new ArrayList<>();

		@Override
		public boolean existsByName(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Crew save(Crew crew) {
			crews.removeIf(existing -> existing.getId().equals(crew.getId()));
			crews.add(crew);
			return crew;
		}

		@Override
		public Optional<Crew> findById(Long crewId) {
			return crews.stream()
				.filter(Crew::isActive)
				.filter(crew -> crew.getId().equals(crewId))
				.findFirst();
		}

		@Override
		public Optional<Crew> findAnyById(Long crewId) {
			return crews.stream()
				.filter(crew -> crew.getId().equals(crewId))
				.findFirst();
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
		}@Override
		public List<Crew> findPublicCrews() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class InMemoryCrewMemberRepository implements CrewMemberRepository {

		private final List<CrewMember> memberships = new ArrayList<>();
		private long sequence = 1L;

		@Override
		public CrewMember save(CrewMember crewMember) {
			if (crewMember.getId() == null) {
				crewMember.assignId(sequence++);
			}
			memberships.removeIf(existing -> existing.getId().equals(crewMember.getId()));
			memberships.add(crewMember);
			return crewMember;
		}

		@Override
		public boolean existsByCrewIdAndUserId(Long crewId, Long userId) {
			return findByCrewIdAndUserId(crewId, userId).isPresent();
		}

		@Override
		public boolean existsLeaderByCrewIdAndUserId(Long crewId, Long userId) {
			return memberships.stream()
				.anyMatch(member ->
					member.getCrewId().equals(crewId)
						&& member.getUserId().equals(userId)
						&& member.isActive()
						&& member.getRole().name().equals("LEADER")
				);
		}

		@Override
		public Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId) {
			return memberships.stream()
				.filter(CrewMember::isActive)
				.filter(member -> member.getCrewId().equals(crewId) && member.getUserId().equals(userId))
				.findFirst();
		}

		@Override
		public Optional<CrewMember> findAnyByCrewIdAndUserId(Long crewId, Long userId) {
			return memberships.stream()
				.filter(member -> member.getCrewId().equals(crewId) && member.getUserId().equals(userId))
				.findFirst();
		}

		@Override
		public List<CrewMember> findAllByCrewId(Long crewId) {
			return memberships.stream()
				.filter(CrewMember::isActive)
				.filter(member -> member.getCrewId().equals(crewId))
				.toList();
		}

		@Override
		public List<CrewMember> findAllByUserId(Long userId) {
			return memberships.stream()
				.filter(CrewMember::isActive)
				.filter(member -> member.getUserId().equals(userId))
				.sorted(Comparator.comparing(CrewMember::getCrewId))
				.toList();
		}
	}

	private static final class InMemoryUserRepository implements UserRepository {
		@Override
		public void withdrawById(Long userId, String anonymizedNickname, java.time.Instant withdrawnAt) {
		}

		@Override
		public boolean updateNickname(Long userId, String nickname) {
			return false;
		}


		private final List<User> users = new ArrayList<>();

		@Override
		public Optional<User> findById(Long userId) {
			return users.stream()
				.filter(user -> user.getId().equals(userId))
				.findFirst();
		}

		@Override
		public boolean existsByNickname(String nickname) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<User> findCompletedUsersByNicknameContaining(String nickname) {
			throw new UnsupportedOperationException();
		}

		@Override
		public User save(User user) {
			users.removeIf(existing -> existing.getId().equals(user.getId()));
			users.add(user);
			return user;
		}
	}
}


