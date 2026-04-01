package com.bangpot.auth.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
@Table(
	name = "auth_users",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_auth_users_provider_provider_id", columnNames = {"provider", "provider_id"}),
		@UniqueConstraint(name = "uk_auth_users_nickname", columnNames = "nickname")
	}
)
public class AuthUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, updatable = false)
	private AuthProvider provider;

	@Column(name = "provider_id", nullable = false, updatable = false)
	private String providerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private AuthUserStatus status;

	@Column(name = "nickname")
	private String nickname;

	@Embedded
	private RequiredTermsAgreement requiredTermsAgreement;

	@Column(name = "pending_redirect_path")
	private String pendingRedirectPath;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected AuthUser() {
	}

	private AuthUser(
		Long id,
		AuthProvider provider,
		String providerId,
		AuthUserStatus status,
		String nickname,
		RequiredTermsAgreement requiredTermsAgreement,
		String pendingRedirectPath,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.provider = provider;
		this.providerId = providerId;
		this.status = status;
		this.nickname = nickname;
		this.requiredTermsAgreement = requiredTermsAgreement;
		this.pendingRedirectPath = pendingRedirectPath;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static AuthUser createTemp(AuthProvider provider, String providerId, String pendingRedirectPath) {
		return new AuthUser(
			null,
			provider,
			providerId,
			AuthUserStatus.TEMP,
			null,
			null,
			pendingRedirectPath,
			null,
			null
		);
	}

	public static AuthUser rehydrate(
		Long id,
		AuthProvider provider,
		String providerId,
		AuthUserStatus status,
		String nickname,
		RequiredTermsAgreement requiredTermsAgreement,
		String pendingRedirectPath,
		Instant createdAt,
		Instant updatedAt
	) {
		return new AuthUser(
			id,
			provider,
			providerId,
			status,
			nickname,
			requiredTermsAgreement,
			pendingRedirectPath,
			createdAt,
			updatedAt
		);
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public void assignId(Long id) {
		this.id = id;
	}

	public boolean requiresCompletion() {
		return status == AuthUserStatus.TEMP;
	}

	public void updatePendingRedirectPath(String redirectPath) {
		if (status == AuthUserStatus.TEMP && redirectPath != null && !redirectPath.isBlank()) {
			pendingRedirectPath = redirectPath;
		}
	}

	public void completeProfile(String nickname, RequiredTermsAgreement agreement) {
		this.nickname = nickname;
		this.requiredTermsAgreement = agreement;
		this.status = AuthUserStatus.FULL;
	}

	public String consumePendingRedirectPathOrDefault(String defaultPath) {
		String nextPath = pendingRedirectPath == null || pendingRedirectPath.isBlank()
			? defaultPath
			: pendingRedirectPath;
		pendingRedirectPath = null;
		return nextPath;
	}

}
