package com.bangpot.crew.error;

import org.springframework.http.HttpStatus;

import com.bangpot.common.error.ApiErrorCode;

public enum CrewErrorCode implements ApiErrorCode {

	CREW_NOT_FOUND(HttpStatus.NOT_FOUND, "크루를 찾을 수 없습니다."),
	CREW_JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "가입 신청을 찾을 수 없습니다."),
	CREW_INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다."),
	CREW_DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 사용 중인 크루명입니다."),
	CREW_JOIN_REQUEST_NOT_ALLOWED(HttpStatus.FORBIDDEN, "비공개 크루는 직접 가입 신청할 수 없습니다."),
	CREW_INVITE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "비공개 크루에서만 직접 초대를 보낼 수 있습니다."),
	CREW_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 가입한 크루입니다."),
	CREW_JOIN_REQUEST_ALREADY_PENDING(HttpStatus.CONFLICT, "이미 가입 신청 대기 중입니다."),
	CREW_INVITE_ALREADY_PENDING(HttpStatus.CONFLICT, "이미 초대 대기 중입니다."),
	CREW_LEADER_LEAVE_NOT_ALLOWED(HttpStatus.CONFLICT, "크루장은 먼저 크루장을 위임한 뒤 탈퇴할 수 있습니다."),
	CREW_LEAVE_NOT_ALLOWED_FOR_HOSTED_MEETING(HttpStatus.CONFLICT, "미완료 생성 모임이 있으면 크루를 탈퇴할 수 없습니다."),
	CREW_TRANSFER_LEADERSHIP_TARGET_NOT_ALLOWED(HttpStatus.CONFLICT, "위임 대상은 현재 일반 크루원만 가능합니다."),
	CREW_MEMBER_REMOVE_TARGET_NOT_ALLOWED(HttpStatus.CONFLICT, "강제 제거 대상은 현재 일반 크루원만 가능합니다.");

	private final HttpStatus status;
	private final String message;

	CrewErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public String code() {
		return name();
	}

	@Override
	public String message() {
		return message;
	}

	@Override
	public HttpStatus status() {
		return status;
	}
}
