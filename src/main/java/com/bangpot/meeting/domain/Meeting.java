package com.bangpot.meeting.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

import com.bangpot.meeting.application.exception.MeetingEditNotAllowedException;
import com.bangpot.meeting.application.exception.MeetingInvalidStatusTransitionException;
import com.bangpot.meeting.application.exception.MeetingResultAlreadyRecordedException;
import com.bangpot.meeting.application.exception.MeetingResultRecordNotAllowedException;

@Entity
@Getter
@Table(name = "meetings")
public class Meeting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "crew_id", nullable = false)
	private Long crewId;

	@Column(name = "host_user_id", nullable = false)
	private Long hostUserId;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "theme_name", nullable = false)
	private String themeName;

	@Column(name = "place", nullable = false)
	private String place;

	@Column(name = "meeting_date", nullable = false)
	private String meetingDate;

	@Column(name = "meeting_time", nullable = false)
	private String meetingTime;

	@Column(name = "capacity", nullable = false)
	private Integer capacity;

	@Column(name = "total_cost")
	private Integer totalCost;

	@Column(name = "contact_link")
	private String contactLink;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private MeetingStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "result", nullable = false)
	private MeetingResult result;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Meeting() {
	}

	private Meeting(
		Long id,
		Long crewId,
		Long hostUserId,
		String title,
		String themeName,
		String place,
		String meetingDate,
		String meetingTime,
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description,
		MeetingStatus status,
		MeetingResult result,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.crewId = crewId;
		this.hostUserId = hostUserId;
		this.title = title;
		this.themeName = themeName;
		this.place = place;
		this.meetingDate = meetingDate;
		this.meetingTime = meetingTime;
		this.capacity = capacity;
		this.totalCost = totalCost;
		this.contactLink = contactLink;
		this.description = description;
		this.status = status;
		this.result = result;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Meeting create(
		Long crewId,
		Long hostUserId,
		String title,
		String themeName,
		String place,
		String meetingDate,
		String meetingTime,
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description
	) {
		return new Meeting(
			null,
			crewId,
			hostUserId,
			title,
			themeName,
			place,
			meetingDate,
			meetingTime,
			capacity,
			totalCost,
			contactLink,
			description,
			MeetingStatus.RECRUITING,
			MeetingResult.NOT_RECORDED,
			null,
			null
		);
	}

	public static Meeting create(
		Long crewId,
		Long hostUserId,
		String title,
		String themeName,
		String place,
		String meetingDate,
		String meetingTime,
		Integer capacity,
		Integer totalCost,
		String reservationLink,
		String openChatLink,
		String description
	) {
		return create(
			crewId,
			hostUserId,
			title,
			themeName,
			place,
			meetingDate,
			meetingTime,
			capacity,
			totalCost,
			openChatLink != null ? openChatLink : reservationLink,
			description
		);
	}

	public static Meeting create(
		Long crewId,
		Long hostUserId,
		String themeName,
		String place,
		String meetingDate,
		String meetingTime,
		Integer capacity,
		Integer totalCost,
		String reservationLink,
		String openChatLink,
		String description
	) {
		return create(
			crewId,
			hostUserId,
			themeName,
			themeName,
			place,
			meetingDate,
			meetingTime,
			capacity,
			totalCost,
			openChatLink != null ? openChatLink : reservationLink,
			description
		);
	}

	public void assignId(Long id) {
		this.id = id;
	}

	public void closeRecruitment() {
		if (status != MeetingStatus.RECRUITING) {
			throw new MeetingInvalidStatusTransitionException(id, status.name(), MeetingStatus.RECRUITMENT_CLOSED.name());
		}
		status = MeetingStatus.RECRUITMENT_CLOSED;
	}

	public void reopenRecruitment() {
		if (status != MeetingStatus.RECRUITMENT_CLOSED) {
			throw new MeetingInvalidStatusTransitionException(id, status.name(), MeetingStatus.RECRUITING.name());
		}
		status = MeetingStatus.RECRUITING;
	}

	public void cancel() {
		if (status != MeetingStatus.RECRUITING && status != MeetingStatus.RECRUITMENT_CLOSED) {
			throw new MeetingInvalidStatusTransitionException(id, status.name(), MeetingStatus.CANCELED.name());
		}
		status = MeetingStatus.CANCELED;
	}

	public void complete() {
		if (status != MeetingStatus.RECRUITMENT_CLOSED) {
			throw new MeetingInvalidStatusTransitionException(id, status.name(), MeetingStatus.COMPLETED.name());
		}
		status = MeetingStatus.COMPLETED;
	}

	public void recordResult(MeetingResult targetResult) {
		if (status != MeetingStatus.COMPLETED) {
			throw new MeetingResultRecordNotAllowedException(id, status.name());
		}
		if (result != MeetingResult.NOT_RECORDED) {
			throw new MeetingResultAlreadyRecordedException(id, result.name());
		}
		result = targetResult;
	}

	public void edit(
		String title,
		String meetingDate,
		String meetingTime,
		String place,
		String themeName,
		Integer capacity,
		Integer totalCost,
		String contactLink,
		String description
	) {
		if (status != MeetingStatus.RECRUITING && status != MeetingStatus.RECRUITMENT_CLOSED) {
			throw new MeetingEditNotAllowedException(id, status.name());
		}
		this.title = title;
		this.meetingDate = meetingDate;
		this.meetingTime = meetingTime;
		this.place = place;
		this.themeName = themeName;
		this.capacity = capacity;
		this.totalCost = totalCost;
		this.contactLink = contactLink;
		this.description = description;
	}

	public void closeRecruitmentAutomatically(LocalDateTime now, long joinedCount) {
		if (status == MeetingStatus.RECRUITING && (joinedCount >= capacity || !now.isBefore(startAt()))) {
			status = MeetingStatus.RECRUITMENT_CLOSED;
		}
	}

	public void completeAutomatically(LocalDateTime now) {
		if (status != MeetingStatus.RECRUITMENT_CLOSED) {
			return;
		}

		if (!now.isBefore(startAt().plusHours(6))) {
			status = MeetingStatus.COMPLETED;
		}
	}

	private LocalDateTime startAt() {
		return LocalDate.parse(meetingDate).atTime(LocalTime.parse(meetingTime));
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
