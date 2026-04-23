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

	MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size);

	long countActiveByMemberUserId(Long userId);

	long countPendingPublicByUserId(Long userId);

	Optional<Crew> findAnyById(Long crewId);

	List<Crew> findPublicCrews();
}
