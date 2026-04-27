package com.bangpot.crew.domain;

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
@Table(name = "crew_invites")
public class CrewInvite {

	public static final String PENDING_CREW_TARGET_UNIQUE_CONSTRAINT = "uk_crew_invites_pending_crew_target_user";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "crew_id", nullable = false)
	private Long crewId;

	@Column(name = "inviter_user_id", nullable = false)
	private Long inviterUserId;

	@Column(name = "target_user_id", nullable = false)
	private Long targetUserId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private CrewInviteStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected CrewInvite() {
	}

	private CrewInvite(
		Long id,
		Long crewId,
		Long inviterUserId,
		Long targetUserId,
		CrewInviteStatus status,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.crewId = crewId;
		this.inviterUserId = inviterUserId;
		this.targetUserId = targetUserId;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static CrewInvite createPending(Long crewId, Long inviterUserId, Long targetUserId) {
		return new CrewInvite(null, crewId, inviterUserId, targetUserId, CrewInviteStatus.PENDING, null, null);
	}

	public void approve() {
		status = CrewInviteStatus.APPROVED;
	}

	public void reject() {
		status = CrewInviteStatus.REJECTED;
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
