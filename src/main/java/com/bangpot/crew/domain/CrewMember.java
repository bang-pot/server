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

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected CrewMember() {
	}

	private CrewMember(Long id, Long crewId, Long userId, CrewRole role, Instant createdAt) {
		this.id = id;
		this.crewId = crewId;
		this.userId = userId;
		this.role = role;
		this.createdAt = createdAt;
	}

	public static CrewMember createLeader(Long crewId, Long userId) {
		return new CrewMember(null, crewId, userId, CrewRole.LEADER, null);
	}

	public static CrewMember createMember(Long crewId, Long userId) {
		return new CrewMember(null, crewId, userId, CrewRole.MEMBER, null);
	}

	public void assignId(Long id) {
		this.id = id;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
