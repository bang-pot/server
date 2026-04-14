package com.bangpot.common.storage;

public record StoredFile(
	String url,
	long sizeBytes,
	String storedName
) {
}
