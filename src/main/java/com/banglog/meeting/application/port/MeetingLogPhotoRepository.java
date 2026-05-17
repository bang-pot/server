package com.banglog.meeting.application.port;

import java.util.List;

import com.banglog.meeting.domain.MeetingLogPhoto;

public interface MeetingLogPhotoRepository {

	List<MeetingLogPhoto> saveAll(List<MeetingLogPhoto> photos);

	List<MeetingLogPhoto> findAllByLogId(Long logId);

	void deleteByLogId(Long logId);
}

