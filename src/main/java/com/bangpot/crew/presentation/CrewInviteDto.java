package com.bangpot.crew.presentation;

final class CrewInviteDto {

	private CrewInviteDto() {
	}

	record MyCrewInviteResponse(Long inviteId, Long crewId, String crewName, String inviterNickname, String status) {
	}

	record ProcessCrewInviteResponse(Long inviteId, Long crewId, String status) {
	}
}
