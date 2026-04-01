package com.bangpot.auth.infrastructure;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.bangpot.auth.infrastructure.config.AuthJwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtAuthTokenService implements AuthSessionTokenService {

	private final AuthJwtProperties authJwtProperties;
	private final Clock clock;
	private final SecretKey secretKey;

	public JwtAuthTokenService(AuthJwtProperties authJwtProperties, Clock clock) {
		this.authJwtProperties = authJwtProperties;
		this.clock = clock;
		this.secretKey = Keys.hmacShaKeyFor(
			authJwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
		);
	}

	@Override
	public String createAccessToken(Long userId) {
		Instant now = Instant.now(clock);
		Instant expiresAt = now.plusSeconds(authJwtProperties.getAccessTokenValiditySeconds());
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.issuer(authJwtProperties.getIssuer())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(secretKey)
			.compact();
	}

	@Override
	public Optional<Long> resolveUserId(String accessToken) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(accessToken)
				.getPayload();
			return Optional.of(Long.parseLong(claims.getSubject()));
		} catch (RuntimeException exception) {
			return Optional.empty();
		}
	}
}
