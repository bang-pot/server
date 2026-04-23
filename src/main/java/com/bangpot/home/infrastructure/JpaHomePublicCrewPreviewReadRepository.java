package com.bangpot.home.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.Crew;
import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;

interface JpaHomePublicCrewPreviewReadRepository extends Repository<Crew, Long> {

	@Query("""
		select
			c.id as crewId,
			c.name as crewName,
			c.imageUrl as coverImageUrl,
			(
				select count(cm.id)
				from CrewMember cm
				where cm.crewId = c.id
				  and cm.status = :activeMemberStatus
			) as memberCount
		from Crew c
		where c.status = :activeCrewStatus
		  and c.visibility = :publicVisibility
		order by c.id desc
		""")
	List<Row> findRows(
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("publicVisibility") CrewVisibility publicVisibility,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		Pageable pageable
	);

	interface Row {
		Long getCrewId();
		String getCrewName();
		String getCoverImageUrl();
		Long getMemberCount();
	}
}
