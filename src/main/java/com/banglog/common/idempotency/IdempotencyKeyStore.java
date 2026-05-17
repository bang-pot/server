package com.banglog.common.idempotency;

import java.time.Duration;

public interface IdempotencyKeyStore {

	boolean reserve(String key, Duration ttl);
}
