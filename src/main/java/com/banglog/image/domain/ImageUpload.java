package com.banglog.image.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "image_uploads")
public class ImageUpload {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "uploader_user_id", nullable = false)
	private Long uploaderUserId;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false, length = 50)
	private ImageUploadCategory category;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ImageUploadStatus status;

	@Column(name = "temp_key", nullable = false, length = 1000)
	private String tempKey;

	@Column(name = "final_key", length = 1000)
	private String finalKey;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "attached_at")
	private Instant attachedAt;

	protected ImageUpload() {
	}

	private ImageUpload(
		Long id,
		Long uploaderUserId,
		ImageUploadCategory category,
		ImageUploadStatus status,
		String tempKey,
		String finalKey,
		long sizeBytes,
		Instant createdAt,
		Instant expiresAt,
		Instant attachedAt
	) {
		this.id = id;
		this.uploaderUserId = uploaderUserId;
		this.category = category;
		this.status = status;
		this.tempKey = tempKey;
		this.finalKey = finalKey;
		this.sizeBytes = sizeBytes;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		this.attachedAt = attachedAt;
	}

	public static ImageUpload createTemp(
		Long uploaderUserId,
		ImageUploadCategory category,
		String tempKey,
		long sizeBytes,
		Instant createdAt,
		Instant expiresAt
	) {
		return new ImageUpload(
			null,
			uploaderUserId,
			category,
			ImageUploadStatus.TEMP,
			tempKey,
			null,
			sizeBytes,
			createdAt,
			expiresAt,
			null
		);
	}

	public void assignId(Long id) {
		if (this.id != null) {
			throw new IllegalStateException("이미지 업로드 id가 이미 할당되어 있습니다.");
		}
		this.id = id;
	}

	public void attach(String finalKey, Instant attachedAt) {
		if (status != ImageUploadStatus.TEMP) {
			throw new IllegalStateException("임시 업로드 상태에서만 이미지를 확정할 수 있습니다.");
		}
		this.status = ImageUploadStatus.ATTACHED;
		this.finalKey = finalKey;
		this.attachedAt = attachedAt;
	}

	public void expire() {
		if (status != ImageUploadStatus.TEMP) {
			throw new IllegalStateException("임시 업로드 상태에서만 만료 처리할 수 있습니다.");
		}
		this.status = ImageUploadStatus.EXPIRED;
	}

	public Long getId() {
		return id;
	}

	public Long getUploaderUserId() {
		return uploaderUserId;
	}

	public ImageUploadCategory getCategory() {
		return category;
	}

	public ImageUploadStatus getStatus() {
		return status;
	}

	public String getTempKey() {
		return tempKey;
	}

	public String getFinalKey() {
		return finalKey;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getAttachedAt() {
		return attachedAt;
	}
}
