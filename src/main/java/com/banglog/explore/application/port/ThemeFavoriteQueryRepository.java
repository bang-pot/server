package com.banglog.explore.application.port;

import com.banglog.explore.domain.view.MyFavoriteThemesSummaryView;
import com.banglog.explore.domain.view.MyFavoriteThemesView;

public interface ThemeFavoriteQueryRepository {

	MyFavoriteThemesView findMyFavoriteThemesViewByUserId(Long userId, int page, int size);

	MyFavoriteThemesSummaryView findMyFavoriteThemesSummaryViewByUserId(Long userId, int limit);
}
