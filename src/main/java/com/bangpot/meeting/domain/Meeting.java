package com.bangpot.meeting.domain;

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
import lombok.Getter;

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

	@Column(name = "reservation_link")
	private String reservationLink;

	@Column(name = "open_chat_link")
	private String openChatLink;

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
		String themeName,
		String place,
		String meetingDate,
		String meetingTime,
		Integer capacity,
		Integer totalCost,
		String reservationLink,
		String openChatLink,
		String description,
		MeetingStatus status,
		MeetingResult result,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.crewId = crewId;
		this.hostUserId = hostUserId;
		this.themeName = themeName;
		this.place = place;
		this.meetingDate = meetingDate;
		this.meetingTime = meetingTime;
		this.capacity = capacity;
		this.totalCost = totalCost;
		this.reservationLink = reservationLink;
		this.openChatLink = openChatLink;
		this.description = description;
		this.status = status;
		this.result = result;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
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
		return new Meeting(
			null,
			crewId,
			hostUserId,
			themeName,
			place,
			meetingDate,
			meetingTime,
			capacity,
			totalCost,
			reservationLink,
			openChatLink,
			description,
			MeetingStatus.RECRUITING,
			MeetingResult.NOT_RECORDED,
			null,
			null
		);
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
