package com.banglog.user.application.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.crew.application.port.CrewQueryRepository;
import com.banglog.user.application.port.UserQueryRepository;
import com.banglog.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.banglog.user.domain.view.MyWithdrawalCheckView;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyWithdrawalCheckService implements GetMyWithdrawalCheckUseCase {

	private final UserQueryRepository userQueryRepository;
	private final CrewQueryRepository crewQueryRepository;

	@Override
	public MyWithdrawalCheckView handle(Query query) {
		if (!userQueryRepository.existsCompletedUser(query.userId())) {
			throw new AccessDeniedException("프로필 완료가 필요합니다.");
		}

		List<MyWithdrawalCheckView.BlockingActiveCrew> blockingActiveCrews =
			crewQueryRepository.findWithdrawalBlockingActiveCrewsByMemberUserId(query.userId());

		return MyWithdrawalCheckView.of(blockingActiveCrews.isEmpty(), blockingActiveCrews);
	}
}
