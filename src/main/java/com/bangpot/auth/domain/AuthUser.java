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
import lombok.Getter;

@Entity
@Getter
@Table(name = "auth_users", uniqueConstraints = {
	@jakarta.persistence.UniqueConstraint(name = "uk_auth_users_provider_provider_id", columnNames = {"provider", "provider_id"})
})
public class AuthUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, updatable = false)
	private AuthProvider provider;

	@Column(name = "provider_id", nullable = false)
	private String providerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private AuthUserStatus status;

	@Embedded
	private RequiredTermsAgreement requiredTermsAgreement;

	@Column(name = "pending_redirect_path")
	private String pendingRedirectPath;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "withdrawn_at")
	private Instant withdrawnAt;

	protected AuthUser() {
	}

	private AuthUser(
		Long id,
		AuthProvider provider,
		String providerId,
		AuthUserStatus status,
		RequiredTermsAgreement requiredTermsAgreement,
		String pendingRedirectPath,
		Instant createdAt,
		Instant updatedAt,
		Instant withdrawnAt
	) {
		this.id = id;
		this.provider = provider;
		this.providerId = providerId;
		this.status = status;
		this.requiredTermsAgreement = requiredTermsAgreement;
		this.pendingRedirectPath = pendingRedirectPath;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.withdrawnAt = withdrawnAt;
	}

	public static AuthUser createTemp(AuthProvider provider, String providerId, String pendingRedirectPath) {
		return new AuthUser(
			null,
			provider,
			providerId,
			AuthUserStatus.TEMP,
			null,
			pendingRedirectPath,
			null,
			null,
			null
		);
	}

	public static AuthUser rehydrate(
		Long id,
		AuthProvider provider,
		String providerId,
		AuthUserStatus status,
		RequiredTermsAgreement requiredTermsAgreement,
		String pendingRedirectPath,
		Instant createdAt,
		Instant updatedAt,
		Instant withdrawnAt
	) {
		return new AuthUser(
			id,
			provider,
			providerId,
			status,
			requiredTermsAgreement,
			pendingRedirectPath,
			createdAt,
			updatedAt,
			withdrawnAt
		);
	}

	public static AuthUser rehydrate(
		Long id,
		AuthProvider provider,
		String providerId,
		AuthUserStatus status,
		RequiredTermsAgreement requiredTermsAgreement,
		String pendingRedirectPath,
		Instant createdAt,
		Instant updatedAt
	) {
		return rehydrate(
			id,
			provider,
			providerId,
			status,
			requiredTermsAgreement,
			pendingRedirectPath,
			createdAt,
			updatedAt,
			null
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

	public boolean isTemp() {
		return status == AuthUserStatus.TEMP;
	}

	public void updatePendingRedirectPath(String redirectPath) {
		if (status == AuthUserStatus.TEMP && redirectPath != null && !redirectPath.isBlank()) {
			pendingRedirectPath = redirectPath;
		}
	}

	public void completeProfile(RequiredTermsAgreement agreement) {
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

	public void withdraw(String tombstoneProviderId, Instant withdrawnAt) {
		this.status = AuthUserStatus.WITHDRAWN;
		this.providerId = tombstoneProviderId;
		this.requiredTermsAgreement = null;
		this.pendingRedirectPath = null;
		this.withdrawnAt = withdrawnAt;
	}
}
