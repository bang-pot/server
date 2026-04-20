package com.bangpot.explore.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bangpot.explore.domain.ThemeFavorite;

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
}
