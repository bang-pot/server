package com.bangpot.common.storage;

public record StoredFile(
	String key,
	String url,
	long sizeBytes,
	String storedName
) {
}
