package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import com.bangpot.explore.domain.ThemeFavorite;
import com.bangpot.user.application.port.FavoriteThemeSummaryReadRepository;

interface JpaFavoriteThemeSummaryReadRepository extends Repository<ThemeFavorite, Long>, FavoriteThemeSummaryReadRepository {

	@Override
	default View load(Long userId, int limit) {
		return View.of(
			findSummaryItems(userId, PageRequest.of(0, limit)).stream()
				.map(row -> Item.of(
					row.getThemeId(),
					row.getThemeName(),
					row.getStoreName(),
					row.getRegionName(),
					row.getThumbnailUrl(),
					row.getFavoriteCount(),
					true
				))
				.toList(),
			countCurrentFavorites(userId)
		);
	}

	@Query("""
		select tf.themeId as themeId,
		       t.name as themeName,
		       s.name as storeName,
		       s.region as regionName,
		       t.posterImageUrl as thumbnailUrl,
		       t.favoriteCount as favoriteCount
		from ThemeFavorite tf, Theme t, Store s
		where tf.userId = :userId
		  and tf.themeId = t.id
		  and t.storeId = s.id
		  and t.active = true
		order by tf.createdAt desc, t.id desc
		""")
	List<FavoriteThemeSummaryProjection> findSummaryItems(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

	@Query("""
		select count(tf)
		from ThemeFavorite tf, Theme t
		where tf.userId = :userId
		  and tf.themeId = t.id
		  and t.active = true
		""")
	Long countCurrentFavorites(@Param("userId") Long userId);

	interface FavoriteThemeSummaryProjection {
		Long getThemeId();

		String getThemeName();

		String getStoreName();

		String getRegionName();

		String getThumbnailUrl();

		Integer getFavoriteCount();
	}
}
