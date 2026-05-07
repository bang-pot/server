package com.bangpot.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockMultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@EnabledIfSystemProperty(named = "s3Smoke", matches = "true")
class S3FileStorageSmokeTest {

	private static final String LOCAL_SECRET_PATH = "src/main/resources/application-local-secret.yml";

	@Test
	void uploadsFileToS3AndExposesItThroughPublicUrl() throws Exception {
		FileStorageProperties properties = loadProperties();
		S3StorageConfig config = new S3StorageConfig();
		S3Client s3Client = config.s3Client(properties);
		S3FileStorage storage = new S3FileStorage(s3Client, properties);
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"s3-smoke.png",
			"image/png",
			"bangpot-s3-smoke".getBytes()
		);

		StoredFile stored = storage.store("smoke", file);
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder(URI.create(stored.url())).GET().build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.body()).isEqualTo("bangpot-s3-smoke");
		} finally {
			s3Client.deleteObject(DeleteObjectRequest.builder()
				.bucket(properties.getS3().getBucket())
				.key(stored.key())
				.build());
			s3Client.close();
		}
	}

	private FileStorageProperties loadProperties() {
		Properties source = loadLocalSecretYaml();
		FileStorageProperties properties = new FileStorageProperties();
		properties.setPublicBaseUrl(required(source, "bangpot.storage.public-base-url"));
		properties.getS3().setBucket(required(source, "bangpot.storage.s3.bucket"));
		properties.getS3().setRegion(optional(source, "bangpot.storage.s3.region", "ap-northeast-2"));
		properties.getS3().setAccessKey(required(source, "bangpot.storage.s3.access-key"));
		properties.getS3().setSecretKey(required(source, "bangpot.storage.s3.secret-key"));
		return properties;
	}

	private Properties loadLocalSecretYaml() {
		FileSystemResource resource = new FileSystemResource(LOCAL_SECRET_PATH);
		assertThat(resource.exists())
			.as("application-local-secret.yml 파일이 필요합니다.")
			.isTrue();
		YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
		factory.setResources(resource);
		Properties properties = factory.getObject();
		assertThat(properties)
			.as("application-local-secret.yml 설정을 읽을 수 있어야 합니다.")
			.isNotNull();
		return properties;
	}

	private String required(Properties properties, String key) {
		String value = properties.getProperty(key);
		assertThat(value)
			.as(key + " 설정이 필요합니다.")
			.isNotBlank();
		return value;
	}

	private String optional(Properties properties, String key, String defaultValue) {
		String value = properties.getProperty(key);
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		return value;
	}
}
