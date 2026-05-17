package com.banglog.user.presentation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banglog.auth.presentation.AuthCookieFactory;
import com.banglog.auth.presentation.UnauthenticatedException;
import com.banglog.common.idempotency.Idempotent;
import com.banglog.crew.domain.view.MyCrewsView;
import com.banglog.crew.domain.view.MyPendingCrewsView;
import com.banglog.explore.domain.view.MyFavoriteThemesSummaryView;
import com.banglog.explore.domain.view.MyFavoriteThemesView;
import com.banglog.meeting.domain.view.MyCalendarView;
import com.banglog.meeting.domain.view.MyCreatedMeetingsView;
import com.banglog.meeting.domain.view.MyJoinedMeetingsView;
import com.banglog.meeting.domain.view.MyMeetingLogsView;
import com.banglog.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.banglog.user.application.usecase.GetMyCalendarUseCase;
import com.banglog.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.banglog.user.application.usecase.GetMyCrewsUseCase;
import com.banglog.user.application.usecase.GetMyFavoriteThemesSummaryUseCase;
import com.banglog.user.application.usecase.GetMyFavoriteThemesUseCase;
import com.banglog.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.banglog.user.application.usecase.GetMyMeetingLogsUseCase;
import com.banglog.user.application.usecase.GetMyPendingCrewsUseCase;
import com.banglog.user.application.usecase.GetMyProfileUseCase;
import com.banglog.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.banglog.user.application.usecase.SearchUsersUseCase;
import com.banglog.user.application.usecase.UpdateMyProfileUseCase;
import com.banglog.user.application.usecase.WithdrawMyAccountUseCase;
import com.banglog.user.domain.view.MyProfileView;
import com.banglog.user.domain.view.MyWithdrawalCheckView;
import com.banglog.user.domain.view.UserSearchView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
class UserController {

	private static final String PAGE_MIN_MESSAGE = "page는 0 이상이어야 합니다.";
	private static final String SIZE_MIN_MESSAGE = "size는 1 이상이어야 합니다.";
	private static final String SIZE_MAX_MESSAGE = "size는 50 이하여야 합니다.";

	private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private final GetMyCalendarUseCase getMyCalendarUseCase;
	private final GetMyCreatedMeetingsUseCase getMyCreatedMeetingsUseCase;
	private final GetMyCrewsUseCase getMyCrewsUseCase;
	private final GetMyFavoriteThemesUseCase getMyFavoriteThemesUseCase;
	private final GetMyFavoriteThemesSummaryUseCase getMyFavoriteThemesSummaryUseCase;
	private final GetMyJoinedMeetingsUseCase getMyJoinedMeetingsUseCase;
	private final GetMyMeetingLogsUseCase getMyMeetingLogsUseCase;
	private final GetMyPendingCrewsUseCase getMyPendingCrewsUseCase;
	private final GetMyProfileUseCase getMyProfileUseCase;
	private final GetMyWithdrawalCheckUseCase getMyWithdrawalCheckUseCase;
	private final SearchUsersUseCase searchUsersUseCase;
	private final UpdateMyProfileUseCase updateMyProfileUseCase;
	private final WithdrawMyAccountUseCase withdrawMyAccountUseCase;
	private final AuthCookieFactory authCookieFactory;

	@GetMapping("/api/users/me")
	ResponseEntity<UserDto.UserProfileResponse> profile(Authentication authentication) {
		MyProfileView result = getMyProfileUseCase.handle(
			GetMyProfileUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/created-meetings")
	ResponseEntity<UserDto.CreatedMeetingsResponse> createdMeetings(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = PAGE_MIN_MESSAGE) int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = SIZE_MIN_MESSAGE)
		@Max(value = 50, message = SIZE_MAX_MESSAGE) int size
	) {
		MyCreatedMeetingsView result = getMyCreatedMeetingsUseCase.handle(
			GetMyCreatedMeetingsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/calendar")
	ResponseEntity<UserDto.CalendarResponse> calendar(Authentication authentication) {
		MyCalendarView result = getMyCalendarUseCase.handle(
			GetMyCalendarUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/favorites/summary")
	ResponseEntity<UserDto.FavoriteThemeSummaryResponse> favoriteThemesSummary(Authentication authentication) {
		MyFavoriteThemesSummaryView result = getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/favorites")
	ResponseEntity<UserDto.FavoriteThemesResponse> favoriteThemes(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = PAGE_MIN_MESSAGE) int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = SIZE_MIN_MESSAGE)
		@Max(value = 50, message = SIZE_MAX_MESSAGE) int size
	) {
		MyFavoriteThemesView result = getMyFavoriteThemesUseCase.handle(
			GetMyFavoriteThemesUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/logs")
	ResponseEntity<UserDto.MyMeetingLogsResponse> myMeetingLogs(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = PAGE_MIN_MESSAGE) int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = SIZE_MIN_MESSAGE)
		@Max(value = 50, message = SIZE_MAX_MESSAGE) int size
	) {
		MyMeetingLogsView result = getMyMeetingLogsUseCase.handle(
			GetMyMeetingLogsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/joined-meetings")
	ResponseEntity<UserDto.JoinedMeetingsResponse> joinedMeetings(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = PAGE_MIN_MESSAGE) int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = SIZE_MIN_MESSAGE)
		@Max(value = 50, message = SIZE_MAX_MESSAGE) int size
	) {
		MyJoinedMeetingsView result = getMyJoinedMeetingsUseCase.handle(
			GetMyJoinedMeetingsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/crews")
	ResponseEntity<UserDto.MyCrewsResponse> myCrews(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = PAGE_MIN_MESSAGE) int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = SIZE_MIN_MESSAGE)
		@Max(value = 50, message = SIZE_MAX_MESSAGE) int size
	) {
		MyCrewsView result = getMyCrewsUseCase.handle(
			GetMyCrewsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/pending-crews")
	ResponseEntity<UserDto.PendingCrewsResponse> myPendingCrews(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = PAGE_MIN_MESSAGE) int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = SIZE_MIN_MESSAGE)
		@Max(value = 50, message = SIZE_MAX_MESSAGE) int size
	) {
		MyPendingCrewsView result = getMyPendingCrewsUseCase.handle(
			GetMyPendingCrewsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/withdrawal-check")
	ResponseEntity<UserDto.WithdrawalCheckResponse> withdrawalCheck(Authentication authentication) {
		MyWithdrawalCheckView result = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/search")
	ResponseEntity<UserDto.UserSearchResponse> searchUsers(
		Authentication authentication,
		@RequestParam("keyword") String keyword,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = PAGE_MIN_MESSAGE) int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = SIZE_MIN_MESSAGE)
		@Max(value = 50, message = SIZE_MAX_MESSAGE) int size
	) {
		UserSearchView result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(requireAuthenticatedUserId(authentication), keyword, page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@PostMapping("/api/users/me/withdrawal")
	@Idempotent
	ResponseEntity<UserDto.WithdrawMyAccountResponse> withdrawMyAccount(
		Authentication authentication,
		@Valid @RequestBody UserDto.WithdrawMyAccountRequest request
	) {
		WithdrawMyAccountUseCase.Result result = withdrawMyAccountUseCase.handle(
			UserDtoMapper.toCommand(requireAuthenticatedUserId(authentication), request)
		);
		SecurityContextHolder.clearContext();
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, authCookieFactory.createLogoutCookieHeader())
			.body(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/nickname-availability")
	ResponseEntity<UserDto.NicknameAvailabilityResponse> nicknameAvailability(
		@RequestParam("nickname") String nickname
	) {
		return ResponseEntity.ok(UserDtoMapper.toResponse(
			checkNicknameAvailabilityUseCase.handle(CheckNicknameAvailabilityUseCase.Query.of(nickname))
		));
	}

	@PatchMapping("/api/users/me")
	@Idempotent
	ResponseEntity<Void> updateProfile(
		Authentication authentication,
		@Valid @RequestBody UserDto.UpdateMyProfileRequest request
	) {
		updateMyProfileUseCase.handle(UserDtoMapper.toCommand(requireAuthenticatedUserId(authentication), request));
		return ResponseEntity.noContent().build();
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}
}
