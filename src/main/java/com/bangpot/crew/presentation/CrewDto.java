package com.bangpot.crew.presentation;

import com.bangpot.crew.domain.CrewJoinViewStatus;
import com.bangpot.crew.domain.CrewRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	record CrewHubResponse(
		Long crewId,
		String name,
		String description,
		String visibility,
		String imageUrl,
		CrewRole myRole,
		boolean hasNotice,
		Integer pendingJoinRequestCount
	) {
	}

	record CrewMemberResponse(
		Long userId,
		String nickname,
		String profileImageUrl,
		String bio,
		String gender,
		int escapeCount,
		CrewRole role,
		String joinedAt
	) {
	}

	record CrewPolicyResponse(
		Long policyId,
		String title,
		String content
	) {
	}

	record UpdateCrewVisibilityRequest(String visibility) {
	}

	record UpdateCrewVisibilityResponse(Long crewId, String visibility) {
	}

	record LeaveCrewResponse(Long crewId) {
	}

	record RemoveCrewMemberResponse(Long crewId, Long removedUserId) {
	}

	record DeleteCrewRequest(
		@NotBlank(message = "삭제할 크루명을 입력해야 합니다.") String crewName
	) {
	}

	record DeleteCrewResponse(Long crewId) {
	}

	record TransferCrewLeadershipRequest(
		@NotNull(message = "위임 대상 크루원을 선택해야 합니다.") Long targetUserId
	) {
	}

	record TransferCrewLeadershipResponse(Long crewId, Long leaderUserId) {
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

	record CrewJoinRequestResponse(
		Long requestId,
		Long userId,
		String nickname,
		String message,
		String status
	) {
	}

	record ApproveCrewJoinRequestResponse(Long crewId, Long requestId, Long userId, CrewRole role) {
	}

	record RejectCrewJoinRequestResponse(Long crewId, Long requestId) {
	}

	record CrewInviteCandidateResponse(Long userId, String nickname) {
	}

	record CreateCrewInviteRequest(@NotNull(message = "초대할 크루원을 선택해야 합니다.") Long targetUserId) {
	}

	record CreateCrewInviteResponse(Long crewId, Long targetUserId, String status) {
	}
}
