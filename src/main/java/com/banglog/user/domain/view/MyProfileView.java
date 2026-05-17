package com.banglog.user.domain.view;

public record MyProfileView(
	Long id,
	String nickname,
	String profileImageUrl,
	Long createdMeetingsCount,
	Long joinedMeetingsCount,
	Long myCrewsCount,
	Long pendingCrewsCount
) {
	public static MyProfileView of(
		Long id,
		String nickname,
		String profileImageUrl,
		Long createdMeetingsCount,
		Long joinedMeetingsCount,
		Long myCrewsCount,
		Long pendingCrewsCount
	) {
		return new MyProfileView(
			id,
			nickname,
			profileImageUrl,
			createdMeetingsCount,
			joinedMeetingsCount,
			myCrewsCount,
			pendingCrewsCount
		);
	}
}
