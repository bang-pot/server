package com.bangpot.user.presentation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bangpot.auth.presentation.AuthCookieFactory;
import com.bangpot.auth.presentation.UnauthenticatedException;
import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.CancelMyPendingCrewJoinRequestUseCase;
import com.bangpot.user.application.usecase.GetMyCalendarUseCase;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesUseCase;
import com.bangpot.user.application.usecase.GetMyFavoriteThemesSummaryUseCase;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyMeetingLogsUseCase;
import com.bangpot.user.application.usecase.GetMyPendingCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.GetMyWithdrawalCheckUseCase;
import com.bangpot.user.application.usecase.SearchUsersUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;
import com.bangpot.user.application.usecase.WithdrawMyAccountUseCase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
class UserController {

	private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private final CancelMyPendingCrewJoinRequestUseCase cancelMyPendingCrewJoinRequestUseCase;
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
		GetMyProfileUseCase.View result = getMyProfileUseCase.handle(
			GetMyProfileUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/created-meetings")
	ResponseEntity<UserDto.CreatedMeetingsResponse> createdMeetings(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		GetMyCreatedMeetingsUseCase.Result result = getMyCreatedMeetingsUseCase.handle(
			GetMyCreatedMeetingsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/calendar")
	ResponseEntity<UserDto.CalendarResponse> calendar(Authentication authentication) {
		GetMyCalendarUseCase.Result result = getMyCalendarUseCase.handle(
			GetMyCalendarUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/favorites/summary")
	ResponseEntity<UserDto.FavoriteThemeSummaryResponse> favoriteThemesSummary(Authentication authentication) {
		GetMyFavoriteThemesSummaryUseCase.Result result = getMyFavoriteThemesSummaryUseCase.handle(
			GetMyFavoriteThemesSummaryUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/favorites")
	ResponseEntity<UserDto.FavoriteThemesResponse> favoriteThemes(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		GetMyFavoriteThemesUseCase.Result result = getMyFavoriteThemesUseCase.handle(
			GetMyFavoriteThemesUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/logs")
	ResponseEntity<UserDto.MyMeetingLogsResponse> myMeetingLogs(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		GetMyMeetingLogsUseCase.Result result = getMyMeetingLogsUseCase.handle(
			GetMyMeetingLogsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/joined-meetings")
	ResponseEntity<UserDto.JoinedMeetingsResponse> joinedMeetings(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page??0 ?댁긽?댁뼱???⑸땲??") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size??1 ?댁긽?댁뼱???⑸땲??")
		@Max(value = 50, message = "size??50 ?댄븯?ъ빞 ?⑸땲??") int size
	) {
		GetMyJoinedMeetingsUseCase.Result result = getMyJoinedMeetingsUseCase.handle(
			GetMyJoinedMeetingsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/crews")
	ResponseEntity<UserDto.MyCrewsResponse> myCrews(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page??0 ?댁긽?댁뼱???⑸땲??") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size??1 ?댁긽?댁뼱???⑸땲??")
		@Max(value = 50, message = "size??50 ?댄븯?ъ빞 ?⑸땲??") int size
	) {
		GetMyCrewsUseCase.Result result = getMyCrewsUseCase.handle(
			GetMyCrewsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/pending-crews")
	ResponseEntity<UserDto.PendingCrewsResponse> myPendingCrews(
		Authentication authentication,
		@RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
		@Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		GetMyPendingCrewsUseCase.Result result = getMyPendingCrewsUseCase.handle(
			GetMyPendingCrewsUseCase.Query.of(requireAuthenticatedUserId(authentication), page, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/me/withdrawal-check")
	ResponseEntity<UserDto.WithdrawalCheckResponse> withdrawalCheck(Authentication authentication) {
		GetMyWithdrawalCheckUseCase.Result result = getMyWithdrawalCheckUseCase.handle(
			GetMyWithdrawalCheckUseCase.Query.of(requireAuthenticatedUserId(authentication))
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@GetMapping("/api/users/search")
	ResponseEntity<UserDto.UserSearchResponse> searchUsers(
		Authentication authentication,
		@RequestParam("keyword") String keyword,
		@RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be at least 1")
		@Max(value = 50, message = "size must be at most 50") int size
	) {
		SearchUsersUseCase.Result result = searchUsersUseCase.handle(
			SearchUsersUseCase.Query.of(requireAuthenticatedUserId(authentication), keyword, size)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	@PostMapping("/api/users/me/withdrawal")
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

	@DeleteMapping("/api/users/me/pending-crews/{joinRequestId}")
	ResponseEntity<UserDto.CancelPendingCrewJoinRequestResponse> cancelMyPendingCrew(
		Authentication authentication,
		@PathVariable Long joinRequestId
	) {
		CancelMyPendingCrewJoinRequestUseCase.Result result = cancelMyPendingCrewJoinRequestUseCase.handle(
			CancelMyPendingCrewJoinRequestUseCase.Command.of(requireAuthenticatedUserId(authentication), joinRequestId)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
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
	ResponseEntity<UserDto.UserProfileResponse> updateProfile(
		Authentication authentication,
		@Valid @RequestBody UserDto.UpdateMyProfileRequest request
	) {
		UpdateMyProfileUseCase.Result result = updateMyProfileUseCase.handle(
			UserDtoMapper.toCommand(requireAuthenticatedUserId(authentication), request)
		);
		return ResponseEntity.ok(UserDtoMapper.toResponse(result));
	}

	private Long requireAuthenticatedUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new UnauthenticatedException();
		}
		return userId;
	}
}
