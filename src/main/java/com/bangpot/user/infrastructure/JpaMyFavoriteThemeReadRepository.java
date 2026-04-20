package com.bangpot.user.infrastructure;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.bangpot.explore.domain.ThemeFavorite;
import com.bangpot.user.application.port.MyFavoriteThemeReadRepository;

interface JpaMyFavoriteThemeReadRepository extends Repository<ThemeFavorite, Long>, MyFavoriteThemeReadRepository {

	@Override
	default SearchResult search(Long userId, int page, int size) {
		List<Row> rows = searchRows(userId, PageRequest.of(page, size + 1));
		boolean hasNext = rows.size() > size;
		List<Row> pageRows = hasNext ? rows.subList(0, size) : rows;
		return SearchResult.of(
			pageRows.stream()
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
			PageInfo.of(page, size, hasNext)
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
	List<Row> searchRows(@Param("userId") Long userId, Pageable pageable);

	interface Row {
		Long getThemeId();

		String getThemeName();

		String getStoreName();

		String getRegionName();

		String getThumbnailUrl();

		Integer getFavoriteCount();
	}
}
