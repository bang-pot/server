package com.bangpot.user.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.crew.application.exception.CrewJoinRequestNotFoundException;
import com.bangpot.crew.domain.CrewJoinRequest;
import com.bangpot.crew.domain.CrewJoinRequestStatus;
import com.bangpot.crew.domain.CrewStatus;
import com.bangpot.crew.domain.CrewVisibility;
import com.bangpot.user.application.port.PendingCrewReadRepository;

interface JpaPendingCrewReadRepository extends Repository<CrewJoinRequest, Long>, PendingCrewReadRepository {

	int MESSAGE_SUMMARY_MAX_LENGTH = 60;

	CrewJoinRequest save(CrewJoinRequest joinRequest);

	@Override
	default SearchResult search(Long userId, int page, int size) {
		List<Row> rows = searchRows(
			userId,
			CrewJoinRequestStatus.PENDING,
			CrewStatus.ACTIVE,
			CrewVisibility.PUBLIC,
			PageRequest.of(page, size + 1)
		);
		boolean hasNext = rows.size() > size;
		List<Row> pageRows = hasNext ? rows.subList(0, size) : rows;
		return SearchResult.of(
			pageRows.stream()
				.map(row -> Item.of(
					row.getJoinRequestId(),
					row.getCrewId(),
					row.getCrewName(),
					row.getRequestedAt().toString(),
					summarizeMessage(row.getMessage())
				))
				.toList(),
			PageInfo.of(page, size, hasNext)
		);
	}

	@Override
	default CancelResult cancel(Long userId, Long joinRequestId) {
		CrewJoinRequest joinRequest = findPendingRequestForUser(
			joinRequestId,
			userId,
			CrewJoinRequestStatus.PENDING
		).orElseThrow(() -> new CrewJoinRequestNotFoundException(joinRequestId));
		joinRequest.cancel();
		save(joinRequest);
		return CancelResult.of(joinRequest.getId(), joinRequest.getCrewId());
	}

	@Query("""
		select
			cjr.id as joinRequestId,
			c.id as crewId,
			c.name as crewName,
			cjr.createdAt as requestedAt,
			cjr.message as message
		from CrewJoinRequest cjr, Crew c
		where cjr.crewId = c.id
		  and cjr.userId = :userId
		  and cjr.status = :pendingStatus
		  and c.status = :activeCrewStatus
		  and c.visibility = :publicVisibility
		order by cjr.createdAt desc, cjr.id desc
		""")
	List<Row> searchRows(
		@Param("userId") Long userId,
		@Param("pendingStatus") CrewJoinRequestStatus pendingStatus,
		@Param("activeCrewStatus") CrewStatus activeCrewStatus,
		@Param("publicVisibility") CrewVisibility publicVisibility,
		Pageable pageable
	);

	@Query("""
		select cjr
		from CrewJoinRequest cjr
		where cjr.id = :joinRequestId
		  and cjr.userId = :userId
		  and cjr.status = :pendingStatus
		""")
	Optional<CrewJoinRequest> findPendingRequestForUser(
		@Param("joinRequestId") Long joinRequestId,
		@Param("userId") Long userId,
		@Param("pendingStatus") CrewJoinRequestStatus pendingStatus
	);

	private static String summarizeMessage(String message) {
		if (message == null) {
			return null;
		}
		String normalized = message.replaceAll("\\s+", " ").trim();
		if (normalized.isBlank()) {
			return null;
		}
		if (normalized.length() <= MESSAGE_SUMMARY_MAX_LENGTH) {
			return normalized;
		}
		return normalized.substring(0, MESSAGE_SUMMARY_MAX_LENGTH);
	}

	interface Row {
		Long getJoinRequestId();
		Long getCrewId();
		String getCrewName();
		Instant getRequestedAt();
		String getMessage();
	}
}
