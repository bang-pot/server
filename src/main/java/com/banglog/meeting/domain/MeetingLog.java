package com.banglog.meeting.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "meeting_logs",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_meeting_logs_meeting_author", columnNames = {"meeting_id", "author_user_id"})
	}
)
public class MeetingLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "meeting_id", nullable = false)
	private Long meetingId;

	@Column(name = "author_user_id", nullable = false)
	private Long authorUserId;

	@Column(nullable = false, length = 1000)
	private String body;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Column(name = "deleted_by_user_id")
	private Long deletedByUserId;

	@Enumerated(EnumType.STRING)
	@Column(name = "deleted_by_role")
	private MeetingLogDeletedBy deletedByRole;

	@Column(name = "delete_reason", length = 500)
	private String deleteReason;

	protected MeetingLog() {
	}

	private MeetingLog(
		Long id,
		Long meetingId,
		Long authorUserId,
		String body,
		Instant createdAt,
		Instant updatedAt,
		Instant deletedAt,
		Long deletedByUserId,
		MeetingLogDeletedBy deletedByRole,
		String deleteReason
	) {
		this.id = id;
		this.meetingId = meetingId;
		this.authorUserId = authorUserId;
		this.body = body;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
		this.deletedByUserId = deletedByUserId;
		this.deletedByRole = deletedByRole;
		this.deleteReason = deleteReason;
	}

	public static MeetingLog create(Long meetingId, Long authorUserId, String body, Instant now) {
		return new MeetingLog(null, meetingId, authorUserId, body, now, now, null, null, null, null);
	}

	public static MeetingLog rehydrate(
		Long id,
		Long meetingId,
		Long authorUserId,
		String body,
		Instant createdAt,
		Instant updatedAt,
		Instant deletedAt,
		Long deletedByUserId,
		MeetingLogDeletedBy deletedByRole,
		String deleteReason
	) {
		return new MeetingLog(
			id,
			meetingId,
			authorUserId,
			body,
			createdAt,
			updatedAt,
			deletedAt,
			deletedByUserId,
			deletedByRole,
			deleteReason
		);
	}

	public void edit(String body, Instant updatedAt) {
		this.body = body;
		this.updatedAt = updatedAt;
	}

	public void delete(Long deletedByUserId, MeetingLogDeletedBy deletedByRole, String deleteReason, Instant deletedAt) {
		this.deletedByUserId = deletedByUserId;
		this.deletedByRole = deletedByRole;
		this.deleteReason = deleteReason;
		this.deletedAt = deletedAt;
		this.updatedAt = deletedAt;
	}

	public void assignId(Long id) {
		if (this.id != null) {
			throw new IllegalStateException("meeting log id is already assigned");
		}
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public Long getMeetingId() {
		return meetingId;
	}

	public Long getAuthorUserId() {
		return authorUserId;
	}

	public String getBody() {
		return body;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public Long getDeletedByUserId() {
		return deletedByUserId;
	}

	public MeetingLogDeletedBy getDeletedByRole() {
		return deletedByRole;
	}

	public String getDeleteReason() {
		return deleteReason;
	}
}
