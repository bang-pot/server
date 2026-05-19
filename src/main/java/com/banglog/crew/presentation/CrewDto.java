package com.banglog.crew.presentation;

import com.banglog.crew.domain.CrewJoinViewStatus;
import com.banglog.crew.domain.CrewRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

final class CrewDto {

	private CrewDto() {
	}

	record CreateCrewRequest(
		@NotBlank(message = "크루명은 비어 있을 수 없습니다.") String name,
		String description,
		String visibility,
		Long imageUploadId
	) {
	}

	record CreateCrewResponse(Long crewId, String name, CrewRole myRole) {
	}

	record PageInfoResponse(
		int page,
		int size,
		boolean hasNext
	) {
	}

	record ExploreCrewCardResponse(
		Long crewId,
		String name,
		String description,
		String imageUrl,
		String visibility,
		String leaderNickname,
		Long memberCount
	) {
	}

	record ExploreCrewCardsResponse(
		java.util.List<ExploreCrewCardResponse> items,
		PageInfoResponse pageInfo
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

	record CrewMembersResponse(
		List<CrewMemberResponse> items,
		PageInfoResponse pageInfo
	) {
	}

	record MeetingCreateCrewResponse(
		Long crewId,
		String crewName
	) {
	}

	record MeetingCreateCrewsResponse(
		java.util.List<MeetingCreateCrewResponse> crews
	) {
	}

	record CrewPolicyResponse(
		Long policyId,
		String title,
		String content
	) {
	}

	record CrewScheduleRequest(
		@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "from은 yyyy-MM-dd 형식이어야 합니다.") String from,
		@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "to는 yyyy-MM-dd 형식이어야 합니다.") String to
	) {
	}

	record CrewScheduleItemResponse(
		Long meetingId,
		String themeName,
		String date,
		String time,
		String meetingStatus,
		String recruitmentStatus,
		String place,
		Long participantCount,
		boolean isCanceled
	) {
	}

	record CrewScheduleResponse(
		java.util.List<CrewScheduleItemResponse> items
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

	record PendingCrewJoinRequestsResponse(
		java.util.List<PendingCrewJoinRequestResponse> items,
		PageInfoResponse pageInfo
	) {
	}

	record CrewJoinRequestResponse(
		Long requestId,
		Long userId,
		String nickname,
		String message,
		String status
	) {
	}

	record CrewJoinRequestsResponse(
		java.util.List<CrewJoinRequestResponse> items,
		PageInfoResponse pageInfo
	) {
	}

	record ApproveCrewJoinRequestResponse(Long crewId, Long requestId, Long userId, CrewRole role) {
	}

	record RejectCrewJoinRequestResponse(Long crewId, Long requestId) {
	}

	record CancelCrewJoinRequestResponse(Long requestId, Long crewId) {
	}

	record CrewInviteCandidateResponse(Long userId, String nickname) {
	}

	record CrewInviteCandidatesResponse(
		List<CrewInviteCandidateResponse> items,
		PageInfoResponse pageInfo
	) {
	}

	record CreateCrewInviteRequest(@NotNull(message = "초대할 크루원을 선택해야 합니다.") Long targetUserId) {
	}

	record CreateCrewInviteResponse(Long crewId, Long targetUserId, String status) {
	}
}
