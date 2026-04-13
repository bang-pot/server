package com.bangpot.crew.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.exception.CrewNotFoundException;
import com.bangpot.crew.application.port.CrewInviteRepository;
import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.crew.application.usecase.GetMyCrewInvitesUseCase;
import com.bangpot.crew.domain.CrewInvite;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyCrewInvitesService implements GetMyCrewInvitesUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final UserRepository userRepository;
	private final CrewRepository crewRepository;
	private final CrewInviteRepository crewInviteRepository;

	@Override
	@Transactional(readOnly = true)
	public List<View> handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "초대 목록을 조회할 수 없습니다.");

		return crewInviteRepository.findByTargetUserId(query.userId()).stream()
			.map(this::toView)
			.toList();
	}

	private View toView(CrewInvite invite) {
		String crewName = crewRepository.findById(invite.getCrewId())
			.orElseThrow(() -> new CrewNotFoundException(invite.getCrewId()))
			.getName();
		String inviterNickname = userRepository.findById(invite.getInviterUserId())
			.orElseThrow(() -> new UserNotFoundException(invite.getInviterUserId()))
			.getNickname();

		return View.of(
			invite.getId(),
			invite.getCrewId(),
			crewName,
			inviterNickname,
			invite.getStatus().name()
		);
	}
}
