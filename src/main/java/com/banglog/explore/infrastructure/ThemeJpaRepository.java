package com.banglog.explore.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banglog.explore.domain.Theme;

interface ThemeJpaRepository extends JpaRepository<Theme, Long> {

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update Theme t
		set t.favoriteCount = t.favoriteCount + 1
		where t.id = :themeId
		  and t.active = true
		""")
	int increaseFavoriteCount(@Param("themeId") Long themeId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update Theme t
		set t.favoriteCount = t.favoriteCount - 1
		where t.id = :themeId
		  and t.active = true
		  and t.favoriteCount > 0
		""")
	int decreaseFavoriteCount(@Param("themeId") Long themeId);

	@Query("""
		select t.favoriteCount
		from Theme t
		where t.id = :themeId
		  and t.active = true
		""")
	Optional<Integer> findActiveFavoriteCountById(@Param("themeId") Long themeId);

	@Query(
		value = """
			select t.id as themeId,
			       t.name as themeName,
			       s.id as storeId,
			       s.name as storeName,
			       s.region as region,
			       s.district as district,
			       t.posterImageUrl as posterImageUrl,
			       t.difficulty as difficulty,
			       t.activityLabel as activityLabel,
			       t.recommendedPlayers as recommendedPlayers,
			       t.runningTimeMinutes as runningTimeMinutes,
			       t.favoriteCount as favoriteCount
			from Theme t, Store s
			where s.id = t.storeId
			  and t.active = true
			  and (
			        :keywordEmpty = true
			        or lower(t.name) like :keywordPattern
			        or lower(s.name) like :keywordPattern
			        or lower(s.region) like :keywordPattern
			        or lower(coalesce(s.district, '')) like :keywordPattern
			      )
			  and (:genresEmpty = true or t.id in :genreThemeIds)
			  and (:regionEmpty = true or s.region = :region)
			  and (:districtEmpty = true or s.district = :district)
			order by t.id desc
			""",
		countQuery = """
			select count(t.id)
			from Theme t, Store s
			where s.id = t.storeId
			  and t.active = true
			  and (
			        :keywordEmpty = true
			        or lower(t.name) like :keywordPattern
			        or lower(s.name) like :keywordPattern
			        or lower(s.region) like :keywordPattern
			        or lower(coalesce(s.district, '')) like :keywordPattern
			      )
			  and (:genresEmpty = true or t.id in :genreThemeIds)
			  and (:regionEmpty = true or s.region = :region)
			  and (:districtEmpty = true or s.district = :district)
			"""
	)
	Page<ThemeCardProjection> search(
		@Param("keywordEmpty") boolean keywordEmpty,
		@Param("keywordPattern") String keywordPattern,
		@Param("genreThemeIds") List<Long> genreThemeIds,
		@Param("genresEmpty") boolean genresEmpty,
		@Param("regionEmpty") boolean regionEmpty,
		@Param("region") String region,
		@Param("districtEmpty") boolean districtEmpty,
		@Param("district") String district,
		Pageable pageable
	);

	@Query(value = """
		select distinct g.name
		from genres g
		join theme_genres tg on tg.genre_id = g.id
		join themes t on t.id = tg.theme_id
		where t.is_active = true
		order by g.name asc
		""", nativeQuery = true)
	List<String> findActiveGenres();

	@Query(value = """
		select tg.theme_id as themeId,
		       g.name as genreName
		from theme_genres tg
		join genres g on g.id = tg.genre_id
		where tg.theme_id in :themeIds
		order by tg.theme_id asc, g.name asc
		""", nativeQuery = true)
	List<ThemeGenreProjection> findGenresByThemeIds(@Param("themeIds") List<Long> themeIds);

	@Query(value = """
		select distinct tg.theme_id
		from theme_genres tg
		join genres g on g.id = tg.genre_id
		where g.name in :genres
		""", nativeQuery = true)
	List<Long> findThemeIdsByGenres(@Param("genres") List<String> genres);

	@Query("""
		select t.id as themeId,
		       t.name as themeName,
		       s.id as storeId,
		       s.name as storeName,
		       s.region as region,
		       s.district as district,
		       t.posterImageUrl as posterImageUrl,
		       t.difficulty as difficulty,
		       t.runningTimeMinutes as runningTimeMinutes,
		       t.description as description,
		       t.externalLink as externalLink
		from Theme t, Store s
		where s.id = t.storeId
		  and t.active = true
		  and t.id = :themeId
		""")
	Optional<ThemeDetailProjection> findActiveThemeDetailById(@Param("themeId") Long themeId);

	@Query("""
		select t.id as themeId,
		       t.name as themeName,
		       s.id as storeId,
		       s.name as storeName,
		       s.region as region,
		       s.district as district,
		       t.posterImageUrl as posterImageUrl,
		       t.difficulty as difficulty,
		       t.runningTimeMinutes as runningTimeMinutes,
		       t.favoriteCount as favoriteCount
		from Theme t, Store s
		where s.id = t.storeId
		  and t.active = true
		  and t.storeId = :storeId
		  and t.id <> :themeId
		order by t.id desc
		""")
	List<RelatedThemeProjection> findRelatedActiveThemes(
		@Param("storeId") Long storeId,
		@Param("themeId") Long themeId,
		Pageable pageable
	);

	@Query("""
		select t.name as themeName,
		       t.posterImageUrl as posterImageUrl
		from Theme t
		where t.active = true
		  and t.name in :themeNames
		order by t.id desc
		""")
	List<ThemePosterProjection> findActivePosterImagesByThemeNames(@Param("themeNames") List<String> themeNames);

	interface ThemeCardProjection {
		Long getThemeId();

		String getThemeName();

		Long getStoreId();

		String getStoreName();

		String getRegion();

		String getDistrict();

		String getPosterImageUrl();

		Integer getDifficulty();

		String getActivityLabel();

		String getRecommendedPlayers();

		Integer getRunningTimeMinutes();

		Integer getFavoriteCount();
	}

	interface ThemeDetailProjection {
		Long getThemeId();

		String getThemeName();

		Long getStoreId();

		String getStoreName();

		String getRegion();

		String getDistrict();

		String getPosterImageUrl();

		Integer getDifficulty();

		Integer getRunningTimeMinutes();

		String getDescription();

		String getExternalLink();
	}

	interface RelatedThemeProjection {
		Long getThemeId();

		String getThemeName();

		Long getStoreId();

		String getStoreName();

		String getRegion();

		String getDistrict();

		String getPosterImageUrl();

		Integer getDifficulty();

		Integer getRunningTimeMinutes();

		Integer getFavoriteCount();
	}

	interface ThemePosterProjection {
		String getThemeName();

		String getPosterImageUrl();
	}

	interface ThemeGenreProjection {
		Long getThemeId();

		String getGenreName();
	}
}
