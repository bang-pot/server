package com.bangpot.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

	StoredFile store(String baseUrl, String category, MultipartFile file);
}
