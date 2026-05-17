package com.banglog.explore.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.banglog.explore.application.port.ThemeFavoriteQueryRepository;
import com.banglog.explore.domain.view.MyFavoriteThemesSummaryView;
import com.banglog.explore.domain.view.MyFavoriteThemesView;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaThemeFavoriteQueryRepository implements ThemeFavoriteQueryRepository {

	private final ThemeFavoriteJpaRepository themeFavoriteJpaRepository;

	@Override
	public MyFavoriteThemesView findMyFavoriteThemesViewByUserId(Long userId, int page, int size) {
		Slice<MyFavoriteThemesView.Item> slice = themeFavoriteJpaRepository.findMyFavoriteThemesViewByUserId(
			userId,
			PageRequest.of(page, size)
		);
		return MyFavoriteThemesView.of(slice.getContent(), MyFavoriteThemesView.Page.of(page, size, slice.hasNext()));
	}

	@Override
	public MyFavoriteThemesSummaryView findMyFavoriteThemesSummaryViewByUserId(Long userId, int limit) {
		List<MyFavoriteThemesSummaryView.Item> items = themeFavoriteJpaRepository.findMyFavoriteThemesSummaryItemsByUserId(
			userId,
			PageRequest.of(0, limit)
		);
		Long totalCount = themeFavoriteJpaRepository.countCurrentFavoritesByUserId(userId);
		return MyFavoriteThemesSummaryView.of(items, totalCount, totalCount > limit);
	}
}
