package com.banglog.scheduler.meeting;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banglog.meeting.application.service.MeetingCompletionService;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(
	prefix = "banglog.meeting.automatic-transition",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true
)
@RequiredArgsConstructor
public class MeetingCompletionScheduler {

	private static final int BATCH_SIZE = 200;

	private final MeetingCompletionService meetingCompletionService;

	@Scheduled(fixedDelayString = "${banglog.meeting.automatic-transition.completion-delay-ms:60000}")
	@Transactional
	public void run() {
		meetingCompletionService.completeTargets(BATCH_SIZE);
	}
}
