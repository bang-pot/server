package com.bangpot.crew.presentation;

final class CrewInviteDto {

	private CrewInviteDto() {
	}

	record MyCrewInviteResponse(Long inviteId, Long crewId, String crewName, String inviterNickname, String status) {
	}

	record MyCrewInvitesResponse(java.util.List<MyCrewInviteResponse> items, PageInfoResponse pageInfo) {
	}

	record PageInfoResponse(int page, int size, boolean hasNext) {
	}

	record ProcessCrewInviteResponse(Long inviteId, Long crewId, String status) {
	}
}
