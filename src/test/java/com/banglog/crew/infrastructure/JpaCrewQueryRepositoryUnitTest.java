package com.banglog.crew.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import com.banglog.crew.domain.CrewMemberStatus;
import com.banglog.crew.domain.CrewRole;
import com.banglog.crew.domain.CrewStatus;
import com.banglog.crew.domain.view.CrewMemberAccessView;
import com.banglog.crew.domain.view.CrewMembersView;
import com.banglog.meeting.domain.MeetingParticipationStatus;
import com.banglog.meeting.domain.MeetingStatus;

@ExtendWith(MockitoExtension.class)
class JpaCrewQueryRepositoryUnitTest {

	@Mock
	private CrewJpaRepository crewJpaRepository;

	@Test
	void crewMembersViewUsesDedicatedAccessQueryInsteadOfCrewHubQuery() {
		JpaCrewQueryRepository repository = new JpaCrewQueryRepository(crewJpaRepository);
		when(crewJpaRepository.findCrewMemberAccessByCrewIdAndUserId(
			1L,
			2L,
			CrewStatus.ACTIVE,
			CrewMemberStatus.ACTIVE
		)).thenReturn(Optional.of(CrewMemberAccessView.of(CrewRole.MEMBER)));
		when(crewJpaRepository.findCrewMemberItemsByCrewId(
			1L,
			CrewMemberStatus.ACTIVE,
			CrewRole.LEADER,
			MeetingStatus.COMPLETED,
			List.of(
				MeetingParticipationStatus.JOINED,
				MeetingParticipationStatus.PENDING,
				MeetingParticipationStatus.APPROVED
			),
			PageRequest.of(0, 20)
		)).thenReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 20), false));

		Optional<CrewMembersView> result = repository.findCrewMembersViewByCrewIdAndUserId(1L, 2L, 0, 20);

		assertThat(result).get().extracting(CrewMembersView::myRole).isEqualTo(CrewRole.MEMBER);
		verify(crewJpaRepository, never()).findCrewHubViewByCrewIdAndUserId(
			1L,
			2L,
			CrewStatus.ACTIVE,
			CrewMemberStatus.ACTIVE,
			CrewRole.LEADER,
			com.banglog.crew.domain.CrewJoinRequestStatus.PENDING
		);
	}
}
