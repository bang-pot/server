package com.bangpot.user.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bangpot.crew.domain.Crew;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.crew.application.port.CrewRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyWithdrawalCheckService implements GetMyWithdrawalCheckUseCase {

	private final UserRepository userRepository;
	private final CrewRepository crewRepository;

	@Override
	public Result handle(Query query) {
		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new AccessDeniedException("프로필 완료가 필요합니다."));

		List<BlockingActiveCrew> blockingActiveCrews = loadBlockingActiveCrews(user.getId());

		return Result.of(
			blockingActiveCrews.isEmpty(),
			blockingActiveCrews,
			List.of()
		);
	}

	private List<BlockingActiveCrew> loadBlockingActiveCrews(Long userId) {
		Map<Long, BlockingActiveCrew> blockingCrews = new LinkedHashMap<>();

		for (Crew crew : crewRepository.findActiveByMemberUserId(userId)) {
			blockingCrews.putIfAbsent(
				crew.getId(),
				BlockingActiveCrew.of(crew.getId(), crew.getName())
			);
		}

		return List.copyOf(blockingCrews.values());
	}
}
