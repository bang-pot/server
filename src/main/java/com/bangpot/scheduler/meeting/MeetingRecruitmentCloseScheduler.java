package com.bangpot.scheduler.meeting;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bangpot.meeting.application.service.MeetingRecruitmentCloseService;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(
	prefix = "banglog.meeting.automatic-transition",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true
)
@RequiredArgsConstructor
public class MeetingRecruitmentCloseScheduler {

	private static final int BATCH_SIZE = 200;

	private final MeetingRecruitmentCloseService meetingRecruitmentCloseService;

	@Scheduled(fixedDelayString = "${banglog.meeting.automatic-transition.recruitment-close-delay-ms:60000}")
	@Transactional
	public void run() {
		meetingRecruitmentCloseService.closeTargets(BATCH_SIZE);
	}
}
