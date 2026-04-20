package com.bangpot.explore.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.bangpot.explore.domain.Theme;

interface ThemeJpaRepository extends JpaRepository<Theme, Long> {

	Optional<Theme> findByIdAndActiveTrue(Long themeId);

	@Query("""
		select t.id as themeId,
		       t.name as themeName,
		       s.id as storeId,
		       s.name as storeName,
		       s.region as region,
		       s.district as district,
		       t.genre as genre,
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
		  and (:genresEmpty = true or t.genre in :genres)
		  and (:regionEmpty = true or s.region = :region)
		  and (:districtEmpty = true or s.district = :district)
		order by t.id desc
		""")
	Slice<ThemeCardProjection> search(
		@Param("keywordEmpty") boolean keywordEmpty,
		@Param("keywordPattern") String keywordPattern,
		@Param("genres") List<String> genres,
		@Param("genresEmpty") boolean genresEmpty,
		@Param("regionEmpty") boolean regionEmpty,
		@Param("region") String region,
		@Param("districtEmpty") boolean districtEmpty,
		@Param("district") String district,
		Pageable pageable
	);

	@Query("""
		select distinct t.genre
		from Theme t
		where t.active = true
		  and t.genre is not null
		order by t.genre asc
		""")
	List<String> findActiveGenres();

	@Query("""
		select t.id as themeId,
		       t.name as themeName,
		       s.id as storeId,
		       s.name as storeName,
		       s.region as region,
		       s.district as district,
		       t.genre as genre,
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
		       t.genre as genre,
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

		String getGenre();

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

		String getGenre();

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

		String getGenre();

		String getPosterImageUrl();

		Integer getDifficulty();

		Integer getRunningTimeMinutes();

		Integer getFavoriteCount();
	}

	interface ThemePosterProjection {
		String getThemeName();

		String getPosterImageUrl();
	}
}
