package com.banglog.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.banglog.crew.application.port.CrewMemberRepository;
import com.banglog.crew.application.port.CrewRepository;
import com.banglog.crew.domain.Crew;
import com.banglog.crew.domain.CrewMember;
import com.banglog.crew.domain.CrewVisibility;
import com.banglog.meeting.application.port.MeetingParticipantRepository;
import com.banglog.meeting.application.port.MeetingRepository;
import com.banglog.meeting.application.usecase.JoinMeetingUseCase;
import com.banglog.meeting.domain.Meeting;
import com.banglog.meeting.domain.MeetingStatus;
import com.banglog.user.application.port.UserRepository;
import com.banglog.user.domain.User;

@SpringBootTest(properties = "spring.datasource.hikari.maximum-pool-size=20")
@ActiveProfiles("test")
class JoinMeetingServiceConcurrencyTest {

	private static final int CAPACITY = 10;
	private static final int CONCURRENT_JOINERS = 100;
	private static final long HOST_USER_ID = 1L;

	@Autowired
	private JoinMeetingUseCase joinMeetingUseCase;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CrewRepository crewRepository;

	@Autowired
	private CrewMemberRepository crewMemberRepository;

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private MeetingParticipantRepository meetingParticipantRepository;

	@Test
	void concurrentJoinRequestsDoNotExceedMeetingCapacity() throws Exception {
		Meeting meeting = prepareRecruitingMeetingWithMembers();
		CountDownLatch ready = new CountDownLatch(CONCURRENT_JOINERS);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_JOINERS);

		try {
			List<Future<JoinAttemptResult>> futures = submitJoinAttempts(executor, meeting, ready, start);

			assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
			start.countDown();

			List<JoinAttemptResult> results = collectResults(futures);

			long successCount = results.stream().filter(JoinAttemptResult::succeeded).count();
			long unexpectedFailureCount = results.stream().filter(JoinAttemptResult::unexpectedFailure).count();
			long joinedParticipantCount = meetingParticipantRepository.countByMeetingId(meeting.getId());
			Meeting updatedMeeting = meetingRepository.findById(meeting.getId()).orElseThrow();

			assertThat(unexpectedFailureCount).isZero();
			assertThat(successCount).isEqualTo(CAPACITY - 1L);
			assertThat(joinedParticipantCount).isEqualTo(CAPACITY - 1L);
			assertThat(joinedParticipantCount + 1L).isLessThanOrEqualTo(CAPACITY);
			assertThat(updatedMeeting.getStatus()).isEqualTo(MeetingStatus.RECRUITMENT_CLOSED);
		} finally {
			executor.shutdownNow();
		}
	}

	private Meeting prepareRecruitingMeetingWithMembers() {
		userRepository.save(User.create(HOST_USER_ID, "host"));
		Crew crew = crewRepository.save(Crew.create("Concurrency Crew", "동시성 테스트 크루", CrewVisibility.PUBLIC, null));
		crewMemberRepository.save(CrewMember.createLeader(crew.getId(), HOST_USER_ID));

		for (long userId = 2L; userId < CONCURRENT_JOINERS + 2L; userId++) {
			userRepository.save(User.create(userId, "member-" + userId));
			crewMemberRepository.save(CrewMember.createMember(crew.getId(), userId));
		}

		return meetingRepository.save(Meeting.create(
			crew.getId(),
			HOST_USER_ID,
			"Concurrency Theme",
			"Gangnam",
			"2099-01-01",
			"19:30",
			CAPACITY,
			null,
			null,
			null,
			null
		));
	}

	private List<Future<JoinAttemptResult>> submitJoinAttempts(
		ExecutorService executor,
		Meeting meeting,
		CountDownLatch ready,
		CountDownLatch start
	) {
		List<Future<JoinAttemptResult>> futures = new ArrayList<>();
		for (long userId = 2L; userId < CONCURRENT_JOINERS + 2L; userId++) {
			futures.add(executor.submit(joinAttempt(meeting, userId, ready, start)));
		}
		return futures;
	}

	private Callable<JoinAttemptResult> joinAttempt(
		Meeting meeting,
		long userId,
		CountDownLatch ready,
		CountDownLatch start
	) {
		return () -> {
			ready.countDown();
			start.await();
			try {
				joinMeetingUseCase.handle(JoinMeetingUseCase.Command.of(meeting.getCrewId(), meeting.getId(), userId));
				return JoinAttemptResult.success();
			} catch (RuntimeException exception) {
				return JoinAttemptResult.failure(exception);
			}
		};
	}

	private List<JoinAttemptResult> collectResults(List<Future<JoinAttemptResult>> futures) throws Exception {
		List<JoinAttemptResult> results = new ArrayList<>();
		for (Future<JoinAttemptResult> future : futures) {
			results.add(future.get(10, TimeUnit.SECONDS));
		}
		return results;
	}

	private record JoinAttemptResult(boolean succeeded, RuntimeException exception) {

		static JoinAttemptResult success() {
			return new JoinAttemptResult(true, null);
		}

		static JoinAttemptResult failure(RuntimeException exception) {
			return new JoinAttemptResult(false, exception);
		}

		boolean unexpectedFailure() {
			return exception != null && exception.getClass().getName().contains("DataAccess");
		}
	}
}
