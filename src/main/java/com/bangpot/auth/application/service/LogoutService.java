package com.bangpot.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.bangpot.auth.application.usecase.LogoutUseCase;
import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

	private final AuthAuditLogger authAuditLogger;

	@Override
	public void handle(Command command) {
		authAuditLogger.logoutSucceeded(command.userId());
	}
}
