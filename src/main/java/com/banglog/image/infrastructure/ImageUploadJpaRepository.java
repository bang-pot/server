package com.banglog.image.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banglog.image.domain.ImageUpload;

import jakarta.persistence.LockModeType;

interface ImageUploadJpaRepository extends JpaRepository<ImageUpload, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select upload from ImageUpload upload where upload.id = :id")
	Optional<ImageUpload> findByIdForUpdate(@Param("id") Long id);
}
