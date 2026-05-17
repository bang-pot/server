package com.bangpot.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

	String store(String category, MultipartFile file);

	void copy(String sourceKey, String targetKey);

	void delete(String key);

	String publicUrl(String key);
}
