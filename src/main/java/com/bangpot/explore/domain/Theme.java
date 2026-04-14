package com.bangpot.explore.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "themes")
public class Theme {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "store_id", nullable = false)
	private Long storeId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "genre", nullable = false)
	private String genre;

	@Column(name = "poster_image_url")
	private String posterImageUrl;

	@Column(name = "difficulty")
	private Integer difficulty;

	@Column(name = "activity_label")
	private String activityLabel;

	@Column(name = "recommended_players")
	private String recommendedPlayers;

	@Column(name = "running_time_minutes")
	private Integer runningTimeMinutes;

	@Column(name = "favorite_count", nullable = false)
	private Integer favoriteCount;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Theme() {
	}

	private Theme(
		Long id,
		Long storeId,
		String name,
		String genre,
		String posterImageUrl,
		Integer difficulty,
		String activityLabel,
		String recommendedPlayers,
		Integer runningTimeMinutes,
		Integer favoriteCount,
		boolean active,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.storeId = storeId;
		this.name = name;
		this.genre = genre;
		this.posterImageUrl = posterImageUrl;
		this.difficulty = difficulty;
		this.activityLabel = activityLabel;
		this.recommendedPlayers = recommendedPlayers;
		this.runningTimeMinutes = runningTimeMinutes;
		this.favoriteCount = favoriteCount;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Theme create(
		Long storeId,
		String name,
		String genre,
		String posterImageUrl,
		Integer difficulty,
		String activityLabel,
		String recommendedPlayers,
		Integer runningTimeMinutes
	) {
		return new Theme(
			null,
			storeId,
			name,
			genre,
			posterImageUrl,
			difficulty,
			activityLabel,
			recommendedPlayers,
			runningTimeMinutes,
			0,
			true,
			null,
			null
		);
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
