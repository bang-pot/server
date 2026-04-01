package com.bangpot.auth.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class RequiredTermsAgreement {

	@Column(name = "required_terms_version")
	private String version;

	@Column(name = "required_terms_accepted_at")
	private Instant acceptedAt;

	protected RequiredTermsAgreement() {
	}

	private RequiredTermsAgreement(String version, Instant acceptedAt) {
		this.version = version;
		this.acceptedAt = acceptedAt;
	}

	public static RequiredTermsAgreement of(String version, Instant acceptedAt) {
		return new RequiredTermsAgreement(version, acceptedAt);
	}
}
