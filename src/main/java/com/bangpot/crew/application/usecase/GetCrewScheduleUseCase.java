package com.bangpot.crew.application.usecase;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import com.bangpot.common.error.ApiErrorField;
import com.bangpot.crew.application.exception.CrewScheduleRequestValidationException;
import com.bangpot.meeting.domain.view.CrewScheduleView;

public interface GetCrewScheduleUseCase {

	long MAX_DATE_RANGE_INCLUSIVE_DAYS = 366;

	CrewScheduleView handle(Query query);

	record Query(
		Long crewId,
		Long userId,
		LocalDate from,
		LocalDate to
	) {
		public static Query of(Long crewId, Long userId, LocalDate from, LocalDate to) {
			Objects.requireNonNull(from, "from");
			Objects.requireNonNull(to, "to");
			if (from.isAfter(to)) {
				throw new CrewScheduleRequestValidationException(
					List.of(new ApiErrorField("to", "종료일은 시작일과 같거나 이후 날짜여야 합니다."))
				);
			}
			if (ChronoUnit.DAYS.between(from, to) >= MAX_DATE_RANGE_INCLUSIVE_DAYS) {
				throw new CrewScheduleRequestValidationException(
					List.of(new ApiErrorField("to", "조회 기간은 최대 1년까지 가능합니다."))
				);
			}
			return new Query(crewId, userId, from, to);
		}
	}

}
