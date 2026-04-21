package com.bangpot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.auth.application.exception.AuthUserNotFoundException;
import com.bangpot.auth.application.port.AuthUserRepository;
import com.bangpot.auth.domain.AuthUser;
import com.bangpot.user.application.exception.UserNotFoundException;
import com.bangpot.user.application.port.CalendarReadRepository;
import com.bangpot.user.application.port.UserRepository;
import com.bangpot.user.application.usecase.GetMyCalendarUseCase;
import com.bangpot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyCalendarService implements GetMyCalendarUseCase {

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final CalendarReadRepository calendarReadRepository;

	@Override
	public Result handle(Query query) {
		AuthUser authUser = authUserRepository.findById(query.userId())
			.orElseThrow(() -> new AuthUserNotFoundException(query.userId()));
		if (authUser.isTemp()) {
			throw new AccessDeniedException("가입 완료 사용자만 이용할 수 있습니다.");
		}

		User user = userRepository.findById(query.userId())
			.orElseThrow(() -> new UserNotFoundException(query.userId()));

		CalendarReadRepository.View view = calendarReadRepository.load(user.getId());
		return Result.of(
			view.items().stream()
				.map(item -> Item.of(
					item.meetingId(),
					item.meetingTitle(),
					item.crewId(),
					item.crewName(),
					item.date(),
					item.time(),
					item.meetingStatus(),
					item.isCanceled(),
					item.participationRole()
				))
				.toList(),
			view.totalCount()
		);
	}
}
