package com.banglog.meeting.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "meeting_log_photos")
public class MeetingLogPhoto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "log_id", nullable = false)
	private Long logId;

	@Column(name = "photo_url", nullable = false, length = 1000)
	private String photoUrl;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected MeetingLogPhoto() {
	}

	private MeetingLogPhoto(Long id, Long logId, String photoUrl, Instant createdAt) {
		this.id = id;
		this.logId = logId;
		this.photoUrl = photoUrl;
		this.createdAt = createdAt;
	}

	public static MeetingLogPhoto create(Long logId, String photoUrl, Instant now) {
		return new MeetingLogPhoto(null, logId, photoUrl, now);
	}

	public static MeetingLogPhoto rehydrate(Long id, Long logId, String photoUrl, Instant createdAt) {
		return new MeetingLogPhoto(id, logId, photoUrl, createdAt);
	}

	public void assignId(Long id) {
		if (this.id != null) {
			throw new IllegalStateException("meeting log photo id is already assigned");
		}
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public Long getLogId() {
		return logId;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}

