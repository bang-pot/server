package com.bangpot.explore.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
@Table(
	name = "theme_favorites",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_theme_favorites_user_theme", columnNames = {"user_id", "theme_id"})
	}
)
public class ThemeFavorite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "theme_id", nullable = false)
	private Long themeId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected ThemeFavorite() {
	}

	private ThemeFavorite(Long userId, Long themeId, Instant createdAt) {
		this.userId = userId;
		this.themeId = themeId;
		this.createdAt = createdAt;
	}

	public static ThemeFavorite create(Long userId, Long themeId, Instant createdAt) {
		return new ThemeFavorite(userId, themeId, createdAt);
	}
}
