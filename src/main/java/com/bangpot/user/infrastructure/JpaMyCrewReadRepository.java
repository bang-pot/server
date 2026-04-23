package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.domain.CrewMemberStatus;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.crew.domain.CrewStatus;

interface JpaMyCrewReadRepository extends Repository<UserJpaEntity, Long> {

	@Query("""
		select
			c.id as crewId,
			c.name as crewName,
			c.visibility as visibility,
			leaderUser.nickname as leaderNickname,
			c.imageUrl as coverImageUrl
		from CrewMember member, Crew c, CrewMember leaderMember, UserJpaEntity leaderUser
		where member.crewId = c.id
		  and leaderMember.crewId = c.id
		  and leaderUser.id = leaderMember.userId
		  and member.userId = :userId
		  and member.status = :activeMemberStatus
		  and c.status = :activeCrewStatus
		  and leaderMember.role = :leaderRole
		  and leaderMember.status = :activeMemberStatus
		order by c.name asc, c.id asc
		""")
	List<Row> searchRows(
		@Param("userId") Long userId,
		@Param("activeMemberStatus") CrewMemberStatus activeMemberStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("leaderRole") CrewRole leaderRole,
		Pageable pageable
	);

	interface Row {
		Long getCrewId();
		String getCrewName();
		String getVisibility();
		String getLeaderNickname();
		String getCoverImageUrl();
	}
}
