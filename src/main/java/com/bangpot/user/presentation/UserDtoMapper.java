package com.bangpot.user.presentation;

import com.bangpot.user.application.usecase.CheckNicknameAvailabilityUseCase;
import com.bangpot.user.application.usecase.GetMyCreatedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyCrewsUseCase;
import com.bangpot.user.application.usecase.GetMyJoinedMeetingsUseCase;
import com.bangpot.user.application.usecase.GetMyProfileUseCase;
import com.bangpot.user.application.usecase.UpdateMyProfileUseCase;

final class UserDtoMapper {

	private UserDtoMapper() {
	}

	static UpdateMyProfileUseCase.Command toCommand(
		Long userId,
		UserDto.UpdateMyProfileRequest request
	) {
		return UpdateMyProfileUseCase.Command.of(userId, request.nickname());
	}

	static UserDto.UserProfileResponse toResponse(GetMyProfileUseCase.View result) {
		return new UserDto.UserProfileResponse(
			result.id(),
			result.nickname(),
			result.profileImageUrl(),
			result.createdMeetingsCount(),
			result.joinedMeetingsCount(),
			result.myCrewsCount(),
			result.pendingCrewsCount()
		);
	}

	static UserDto.UserProfileResponse toResponse(UpdateMyProfileUseCase.Result result) {
		return new UserDto.UserProfileResponse(result.id(), result.nickname(), null, null, null, null, null);
	}

	static UserDto.CreatedMeetingsResponse toResponse(GetMyCreatedMeetingsUseCase.Result result) {
		return new UserDto.CreatedMeetingsResponse(
			result.items().stream()
				.map(item -> new UserDto.CreatedMeetingItemResponse(
					item.meetingId(),
					item.title(),
					item.status(),
					item.date(),
					item.time(),
					item.crewId(),
					item.crewName()
				))
				.toList(),
			new UserDto.CreatedMeetingsPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}

	static UserDto.JoinedMeetingsResponse toResponse(GetMyJoinedMeetingsUseCase.Result result) {
		return new UserDto.JoinedMeetingsResponse(
			result.items().stream()
				.map(item -> new UserDto.JoinedMeetingItemResponse(
					item.meetingId(),
					item.title(),
					item.themeName(),
					item.crewId(),
					item.crewName(),
					item.date(),
					item.time(),
					item.status(),
					item.result(),
					item.canWriteReview()
				))
				.toList(),
			new UserDto.JoinedMeetingsPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}

	static UserDto.MyCrewsResponse toResponse(GetMyCrewsUseCase.Result result) {
		return new UserDto.MyCrewsResponse(
			result.items().stream()
				.map(item -> new UserDto.MyCrewItemResponse(
					item.crewId(),
					item.crewName(),
					item.visibility(),
					item.leaderNickname(),
					item.coverImageUrl()
				))
				.toList(),
			new UserDto.MyCrewsPageInfo(
				result.pageInfo().page(),
				result.pageInfo().size(),
				result.pageInfo().hasNext()
			)
		);
	}

	static UserDto.NicknameAvailabilityResponse toResponse(
		CheckNicknameAvailabilityUseCase.Result result
	) {
		return new UserDto.NicknameAvailabilityResponse(result.nickname(), result.available());
	}
}
