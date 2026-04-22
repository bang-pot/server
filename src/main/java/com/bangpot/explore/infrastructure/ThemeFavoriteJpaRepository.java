package com.bangpot.explore.infrastructure;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.explore.domain.ThemeFavorite;
import com.bangpot.explore.domain.view.MyFavoriteThemesSummaryView;
import com.bangpot.explore.domain.view.MyFavoriteThemesView;

interface ThemeFavoriteJpaRepository extends JpaRepository<ThemeFavorite, Long> {

	boolean existsByUserIdAndThemeId(Long userId, Long themeId);

	@Modifying
	long deleteByUserIdAndThemeId(Long userId, Long themeId);

	@Query("""
		select tf.themeId
		from ThemeFavorite tf
		where tf.userId = :userId
		  and tf.themeId in :themeIds
		""")
	List<Long> findThemeIdsByUserIdAndThemeIdIn(@Param("userId") Long userId, @Param("themeIds") List<Long> themeIds);

	@Query("""
		select new com.bangpot.explore.domain.view.MyFavoriteThemesView.Item(
			tf.themeId,
			t.name,
			s.name,
			s.region,
			t.posterImageUrl,
			t.favoriteCount,
			true
		)
		from ThemeFavorite tf, Theme t, Store s
		where tf.userId = :userId
		  and tf.themeId = t.id
		  and t.storeId = s.id
		  and t.active = true
		order by tf.createdAt desc, t.id desc
		""")
	Slice<MyFavoriteThemesView.Item> findMyFavoriteThemesViewByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("""
		select new com.bangpot.explore.domain.view.MyFavoriteThemesSummaryView.Item(
			tf.themeId,
			t.name,
			s.name,
			s.region,
			t.posterImageUrl,
			t.favoriteCount,
			true
		)
		from ThemeFavorite tf, Theme t, Store s
		where tf.userId = :userId
		  and tf.themeId = t.id
		  and t.storeId = s.id
		  and t.active = true
		order by tf.createdAt desc, t.id desc
		""")
	List<MyFavoriteThemesSummaryView.Item> findMyFavoriteThemesSummaryItemsByUserId(
		@Param("userId") Long userId,
		Pageable pageable
	);

	@Query("""
		select count(tf)
		from ThemeFavorite tf, Theme t
		where tf.userId = :userId
		  and tf.themeId = t.id
		  and t.active = true
		""")
	Long countCurrentFavoritesByUserId(@Param("userId") Long userId);
}
