package com.bangpot.explore.application.port;

import com.bangpot.explore.domain.view.MyFavoriteThemesSummaryView;
import com.bangpot.explore.domain.view.MyFavoriteThemesView;

public interface ThemeFavoriteQueryRepository {

	MyFavoriteThemesView findMyFavoriteThemesViewByUserId(Long userId, int page, int size);

	MyFavoriteThemesSummaryView findMyFavoriteThemesSummaryViewByUserId(Long userId, int limit);
}
