package com.bangpot.crew.application.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewMemberRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetCrewMembersUseCase;
import com.bangpot.crew.domain.CrewMember;
import com.bangpot.crew.domain.CrewRole;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCrewMembersService implements GetCrewMembersUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final UserRepository userRepository;
	private final CrewRepository crewRepository;
	private final CrewMemberRepository crewMemberRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle(Query query) {
		crewRepository.findById(query.crewId())
			.orElseThrow(() -> new CrewNotFoundException(query.crewId()));

		completedUserAccessService.validateCompletedUser(query.userId(), "가입한 크루원만 크루원 목록을 조회할 수 있습니다.");
		if (crewMemberRepository.findByCrewIdAndUserId(query.crewId(), query.userId()).isEmpty()) {
			throw new AccessDeniedException("가입한 크루원만 크루원 목록을 조회할 수 있습니다.");
		}

		return crewMemberRepository.findAllByCrewId(query.crewId()).stream()
			.sorted(Comparator
				.comparing((CrewMember member) -> member.getRole() != CrewRole.LEADER)
				.thenComparing(CrewMember::getCreatedAt, Comparator.reverseOrder()))
			.map(this::toView)
			.toList();
	}

	private View toView(CrewMember member) {
		User user = userRepository.findById(member.getUserId())
			.orElseThrow(() -> new UserNotFoundException(member.getUserId()));

		return View.of(
			user.getId(),
			user.getNickname(),
			null,
			null,
			null,
			0,
			member.getRole(),
			member.getCreatedAt().toString()
		);
	}
}
