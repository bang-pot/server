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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
@Table(
	name = "crew_members",
	uniqueConstraints = @UniqueConstraint(name = "uk_crew_members_crew_user", columnNames = {"crew_id", "user_id"})
)
public class CrewMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "crew_id", nullable = false)
	private Long crewId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private CrewRole role;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private CrewMemberStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected CrewMember() {
	}

	private CrewMember(Long id, Long crewId, Long userId, CrewRole role, CrewMemberStatus status, Instant createdAt) {
		this.id = id;
		this.crewId = crewId;
		this.userId = userId;
		this.role = role;
		this.status = status;
		this.createdAt = createdAt;
	}

	public static CrewMember createLeader(Long crewId, Long userId) {
		return new CrewMember(null, crewId, userId, CrewRole.LEADER, CrewMemberStatus.ACTIVE, null);
	}

	public static CrewMember createMember(Long crewId, Long userId) {
		return new CrewMember(null, crewId, userId, CrewRole.MEMBER, CrewMemberStatus.ACTIVE, null);
	}

	public void assignId(Long id) {
		this.id = id;
	}

	public void transferLeadershipToLeader() {
		this.role = CrewRole.LEADER;
		this.status = CrewMemberStatus.ACTIVE;
	}

	public void transferLeadershipToMember() {
		this.role = CrewRole.MEMBER;
		this.status = CrewMemberStatus.ACTIVE;
	}

	public void leave() {
		this.status = CrewMemberStatus.LEFT;
	}

	public void remove() {
		this.status = CrewMemberStatus.REMOVED;
	}

	public void reactivateAsMember() {
		this.role = CrewRole.MEMBER;
		this.status = CrewMemberStatus.ACTIVE;
		this.createdAt = Instant.now();
	}

	public boolean isActive() {
		return status == CrewMemberStatus.ACTIVE;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
