package com.banglog.user.infrastructure;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "users")
class UserJpaEntity {

	@Id
	private Long id;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "bio")
	private String bio;

	@Column(name = "gender")
	private String gender;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "withdrawn_at")
	private Instant withdrawnAt;

	protected UserJpaEntity() {
	}

	private UserJpaEntity(
		Long id,
		String nickname,
		String bio,
		String gender,
		String profileImageUrl,
		Instant createdAt,
		Instant updatedAt,
		Instant withdrawnAt
	) {
		this.id = id;
		this.nickname = nickname;
		this.bio = bio;
		this.gender = gender;
		this.profileImageUrl = profileImageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.withdrawnAt = withdrawnAt;
	}

	static UserJpaEntity create(Long id, String nickname) {
		return new UserJpaEntity(id, nickname, null, null, null, null, null, null);
	}

	void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	void updateProfile(String nickname, String profileImageUrl) {
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
	}

	void withdraw(String anonymizedNickname, Instant withdrawnAt) {
		this.nickname = anonymizedNickname;
		this.bio = null;
		this.gender = null;
		this.profileImageUrl = null;
		this.withdrawnAt = withdrawnAt;
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
