package com.banglog.meeting.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banglog.meeting.domain.MeetingLogPhoto;

interface MeetingLogPhotoJpaRepository extends JpaRepository<MeetingLogPhoto, Long> {

	List<MeetingLogPhoto> findAllByLogIdOrderByIdAsc(Long logId);

	void deleteByLogId(Long logId);
}

