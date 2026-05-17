package com.banglog.meeting.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.banglog.meeting.application.port.MeetingLogPhotoRepository;
import com.banglog.meeting.domain.MeetingLogPhoto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class JpaMeetingLogPhotoRepository implements MeetingLogPhotoRepository {

	private final MeetingLogPhotoJpaRepository meetingLogPhotoJpaRepository;

	@Override
	public List<MeetingLogPhoto> saveAll(List<MeetingLogPhoto> photos) {
		return meetingLogPhotoJpaRepository.saveAll(photos);
	}

	@Override
	public List<MeetingLogPhoto> findAllByLogId(Long logId) {
		return meetingLogPhotoJpaRepository.findAllByLogIdOrderByIdAsc(logId);
	}

	@Override
	public void deleteByLogId(Long logId) {
		meetingLogPhotoJpaRepository.deleteByLogId(logId);
	}
}

