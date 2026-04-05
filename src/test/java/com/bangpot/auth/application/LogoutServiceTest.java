package com.bangpot.auth.application;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import com.bangpot.auth.application.service.LogoutService;
import com.bangpot.auth.infrastructure.logging.AuthAuditLogger;

class LogoutServiceTest {

	@Test
	void writesAuditLogForAuthenticatedLogout() {
		AuthAuditLogger authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		LogoutService logoutService = new LogoutService(authAuditLogger);

		logoutService.handle(com.bangpot.auth.application.usecase.LogoutUseCase.Command.of(77L));

		verify(authAuditLogger).logoutSucceeded(77L);
	}

	@Test
	void writesAnonymousAuditLogWhenUserIsMissing() {
		AuthAuditLogger authAuditLogger = org.mockito.Mockito.mock(AuthAuditLogger.class);
		LogoutService logoutService = new LogoutService(authAuditLogger);

		logoutService.handle(com.bangpot.auth.application.usecase.LogoutUseCase.Command.of(null));

		verify(authAuditLogger).logoutSucceeded(null);
	}
}
