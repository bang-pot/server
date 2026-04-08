package com.bangpot.crew.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.service.GetPublicCrewCardsService;
import com.bangpot.crew.application.usecase.GetPublicCrewCardsUseCase;
import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewVisibility;

class CrewPublicCardUseCaseServicesTest {

	private InMemoryCrewRepository crewRepository;
	private GetPublicCrewCardsUseCase getPublicCrewCardsUseCase;

	@BeforeEach
	void setUp() {
		crewRepository = new InMemoryCrewRepository();
		getPublicCrewCardsUseCase = new GetPublicCrewCardsService(crewRepository);
	}

	@Test
	void returnsOnlyPublicCrewsForCardList() {
		crewRepository.save(Crew.create("Crew Alpha", "public crew", CrewVisibility.PUBLIC, null));
		crewRepository.save(Crew.create("Crew Private", "private crew", CrewVisibility.PRIVATE, null));
		crewRepository.save(Crew.create("Crew Beta", "night runners", CrewVisibility.PUBLIC, "https://image.example/beta.png"));

		List<GetPublicCrewCardsUseCase.View> result = getPublicCrewCardsUseCase.handle();

		assertThat(result)
			.extracting(GetPublicCrewCardsUseCase.View::name)
			.containsExactly("Crew Alpha", "Crew Beta");
		assertThat(result)
			.extracting(GetPublicCrewCardsUseCase.View::visibility)
			.containsOnly("PUBLIC");
		assertThat(result.get(1).imageUrl()).isEqualTo("https://image.example/beta.png");
	}

	private static final class InMemoryCrewRepository implements CrewRepository {

		private final List<Crew> crews = new ArrayList<>();
		private long sequence = 1L;

		@Override
		public boolean existsByName(String name) {
			return crews.stream().anyMatch(crew -> name.equals(crew.getName()));
		}

		@Override
		public Crew save(Crew crew) {
			if (crew.getId() == null) {
				crew.assignId(sequence++);
			}
			crews.add(crew);
			return crew;
		}

		@Override
		public java.util.Optional<Crew> findById(Long crewId) {
			return crews.stream().filter(crew -> crewId.equals(crew.getId())).findFirst();
		}

		@Override
		public List<Crew> findPublicCrews() {
			return crews.stream()
				.filter(crew -> crew.getVisibility() == CrewVisibility.PUBLIC)
				.toList();
		}
	}
}
