package com.bangpot.crew.presentation;

import com.bangpot.crew.domain.CrewJoinViewStatus;
import com.bangpot.crew.domain.CrewRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

final class CrewDto {

	private CrewDto() {
	}

	record CreateCrewRequest(
		@NotBlank(message = "크루명은 비어 있을 수 없습니다.") String name,
		String description,
		String visibility,
		String imageUrl
	) {
	}

	record CreateCrewResponse(Long crewId, String name, CrewRole myRole) {
	}

	record PublicCrewCardResponse(
		Long crewId,
		String name,
		String description,
		String visibility,
		String imageUrl
	) {
	}

	record CrewJoinViewResponse(
		Long crewId,
		String name,
		String description,
		String visibility,
		String imageUrl,
		CrewJoinViewStatus myStatus
	) {
	}

	record RequestCrewJoinRequest(
		@Size(max = 200, message = "신청 메시지는 200자 이하여야 합니다.") String message
	) {
	}

	record RequestCrewJoinResponse(Long crewId, CrewJoinViewStatus myStatus) {
	}

	record PendingCrewJoinRequestResponse(Long requestId, Long userId, String nickname) {
	}

	record ApproveCrewJoinRequestResponse(Long crewId, Long requestId, Long userId, CrewRole role) {
	}

	record RejectCrewJoinRequestResponse(Long crewId, Long requestId) {
	}
}
