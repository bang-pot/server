package com.banglog.crew.application.port;

import java.util.List;
import java.util.Optional;

import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.view.MyCrewsView;

public interface CrewRepository {

	boolean existsByName(String name);

	Crew save(Crew crew);

	Optional<Crew> findById(Long crewId);

	Optional<Crew> findByIdForUpdate(Long crewId);

	Optional<Crew> findByIdForShare(Long crewId);

	List<Crew> findActiveByMemberUserId(Long userId);

	MyCrewsView findMyCrewsViewByMemberUserId(Long userId, int page, int size);

	long countActiveByMemberUserId(Long userId);

	long countPendingPublicByUserId(Long userId);

	Optional<Crew> findAnyById(Long crewId);
}
