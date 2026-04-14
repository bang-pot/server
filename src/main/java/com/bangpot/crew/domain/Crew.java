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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
@Table(
	name = "crews",
	uniqueConstraints = @UniqueConstraint(name = "uk_crews_name", columnNames = "name")
)
public class Crew {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "visibility", nullable = false)
	private CrewVisibility visibility;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private CrewStatus status;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Crew() {
	}

	private Crew(
		Long id,
		String name,
		String description,
		CrewVisibility visibility,
		CrewStatus status,
		String imageUrl,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.visibility = visibility;
		this.status = status;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Crew create(String name, String description, CrewVisibility visibility, String imageUrl) {
		return new Crew(
			null,
			name,
			description,
			visibility == null ? CrewVisibility.PUBLIC : visibility,
			CrewStatus.ACTIVE,
			imageUrl,
			null,
			null
		);
	}

	public boolean allowsDirectJoinRequest() {
		return visibility == CrewVisibility.PUBLIC;
	}

	public boolean allowsDirectInvite() {
		return visibility == CrewVisibility.PRIVATE;
	}

	public void changeVisibility(CrewVisibility visibility) {
		if (visibility == null) {
			throw new IllegalArgumentException("visibility must not be null");
		}
		this.visibility = visibility;
	}

	public void delete() {
		this.status = CrewStatus.DELETED;
	}

	public boolean isActive() {
		return status == CrewStatus.ACTIVE;
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
