package com.banglog.crew.domain;

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
import lombok.Getter;

@Entity
@Getter
@Table(name = "crew_join_requests")
public class CrewJoinRequest {

	public static final String PENDING_CREW_USER_UNIQUE_CONSTRAINT = "uk_crew_join_requests_pending_crew_user";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "crew_id", nullable = false)
	private Long crewId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "message", length = 200)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private CrewJoinRequestStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected CrewJoinRequest() {
	}

	private CrewJoinRequest(
		Long id,
		Long crewId,
		Long userId,
		String message,
		CrewJoinRequestStatus status,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.crewId = crewId;
		this.userId = userId;
		this.message = message;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static CrewJoinRequest createPending(Long crewId, Long userId, String message) {
		return new CrewJoinRequest(null, crewId, userId, message, CrewJoinRequestStatus.PENDING, null, null);
	}

	public void approve() {
		status = CrewJoinRequestStatus.APPROVED;
	}

	public void reject() {
		status = CrewJoinRequestStatus.REJECTED;
	}

	public void cancel() {
		status = CrewJoinRequestStatus.CANCELED;
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
