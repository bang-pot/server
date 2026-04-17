package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.port.WithdrawalCheckReadRepository;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyWithdrawalCheckService implements GetMyWithdrawalCheckUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final WithdrawalCheckReadRepository withdrawalCheckReadRepository;

	@Override
	public Result handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.requiresCompletion()) {
			throw new AccessDeniedException("full user profile is required");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));

		WithdrawalCheckReadRepository.View view = withdrawalCheckReadRepository.load(user.getId());
		return Result.of(
			view.blockingActiveCrews().isEmpty() && view.blockingParticipatingMeetings().isEmpty(),
			view.blockingActiveCrews().stream()
				.map(crew -> BlockingActiveCrew.of(crew.crewId(), crew.crewName()))
				.toList(),
			view.blockingParticipatingMeetings().stream()
				.map(meeting -> BlockingParticipatingMeeting.of(
					meeting.meetingId(),
					meeting.meetingTitle(),
					meeting.crewId(),
					meeting.crewName(),
					meeting.meetingStatus(),
					meeting.date(),
					meeting.time(),
					meeting.participationRole()
				))
				.toList()
		);
	}
}
