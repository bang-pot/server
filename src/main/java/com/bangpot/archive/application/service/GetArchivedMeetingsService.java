package com.bangpot.archive.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bangpot.archive.application.port.ArchiveMeetingReadRepository;
import com.bangpot.archive.application.usecase.GetArchivedMeetingsUseCase;
import com.bangpot.explore.application.port.ExploreThemeReadRepository;
import com.bangpot.user.application.service.CompletedUserAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetArchivedMeetingsService implements GetArchivedMeetingsUseCase {

	private final CompletedUserAccessService completedUserAccessService;
	private final ArchiveMeetingReadRepository archiveMeetingReadRepository;
	private final ExploreThemeReadRepository exploreThemeReadRepository;

	@Override
	public Result handle(Query query) {
		completedUserAccessService.validateCompletedUser(query.userId(), "완료된 모임 아카이브를 조회할 수 없습니다.");

		ArchiveMeetingReadRepository.SearchResult searchResult = archiveMeetingReadRepository.search(
			query.userId(),
			query.page(),
			query.size()
		);
		Map<String, String> postersByThemeName = exploreThemeReadRepository.getPosterImageUrlsByThemeNames(
			searchResult.items().stream()
				.map(ArchiveMeetingReadRepository.Item::themeName)
				.distinct()
				.toList()
		);

		List<Item> items = searchResult.items().stream()
			.map(item -> Item.of(
				item.meetingId(),
				item.crewId(),
				item.crewName(),
				item.themeName(),
				item.place(),
				item.date(),
				item.result(),
				postersByThemeName.get(item.themeName())
			))
			.toList();

		return Result.of(
			items,
			PageInfo.of(
				searchResult.pageInfo().page(),
				searchResult.pageInfo().size(),
				searchResult.pageInfo().hasNext()
			)
		);
	}
}
