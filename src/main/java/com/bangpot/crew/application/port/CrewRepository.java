package com.bangpot.crew.application.port;

import java.util.List;
import java.util.Optional;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.view.MyCrewsView;

public interface CrewRepository {

	boolean existsByName(String name);

	Crew save(Crew crew);

	Optional<Crew> findById(Long crewId);

	List<Crew> findActiveByMemberUserId(Long userId);

	default MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size) {
		return MyCrewsView.of(List.of(), MyCrewsView.Page.of(page, size, false));
	}

	long countActiveByMemberUserId(Long userId);

	long countPendingPublicByUserId(Long userId);

	default Optional<Crew> findAnyById(Long crewId) {
		return findById(crewId);
	}

	List<Crew> findPublicCrews();
}
