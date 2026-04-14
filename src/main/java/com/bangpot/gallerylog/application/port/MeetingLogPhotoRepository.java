package com.bangpot.gallerylog.application.port;

import java.util.List;

import com.bangpot.gallerylog.domain.MeetingLogPhoto;

public interface MeetingLogPhotoRepository {

	List<MeetingLogPhoto> saveAll(List<MeetingLogPhoto> photos);

	List<MeetingLogPhoto> findAllByLogId(Long logId);

	void deleteByLogId(Long logId);
}
