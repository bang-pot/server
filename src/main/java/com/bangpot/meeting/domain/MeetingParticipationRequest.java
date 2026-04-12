package com.bangpot.meeting.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
@Table(
	name = "meeting_participation_requests",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_meeting_participation_requests_meeting_user",
		columnNames = {"meeting_id", "user_id"}
	)
)
public class MeetingParticipationRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "meeting_id", nullable = false)
	private Long meetingId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private MeetingParticipationStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected MeetingParticipationRequest() {
	}

	private MeetingParticipationRequest(
		Long id,
		Long meetingId,
		Long userId,
		MeetingParticipationStatus status,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.meetingId = meetingId;
		this.userId = userId;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static MeetingParticipationRequest createPending(Long meetingId, Long userId) {
		return new MeetingParticipationRequest(null, meetingId, userId, MeetingParticipationStatus.PENDING, null, null);
	}

	public static MeetingParticipationRequest rehydrate(
		Long id,
		Long meetingId,
		Long userId,
		MeetingParticipationStatus status,
		Instant createdAt,
		Instant updatedAt
	) {
		return new MeetingParticipationRequest(id, meetingId, userId, status, createdAt, updatedAt);
	}

	public void assignId(Long id) {
		this.id = id;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
