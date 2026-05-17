package com.banglog.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

class S3StorageConfigTest {

	@Test
	void usesConfiguredStaticCredentialsWhenAccessKeyAndSecretKeyArePresent() {
		FileStorageProperties properties = new FileStorageProperties();
		properties.getS3().setAccessKey("local-access-key");
		properties.getS3().setSecretKey("local-secret-key");
		S3StorageConfig config = new S3StorageConfig();

		AwsCredentials credentials = config.credentialsProvider(properties).resolveCredentials();

		assertThat(credentials).isInstanceOf(AwsBasicCredentials.class);
		assertThat(credentials.accessKeyId()).isEqualTo("local-access-key");
		assertThat(credentials.secretAccessKey()).isEqualTo("local-secret-key");
	}

	@Test
	void usesDefaultCredentialsProviderWhenStaticCredentialsAreMissing() {
		FileStorageProperties properties = new FileStorageProperties();
		S3StorageConfig config = new S3StorageConfig();

		assertThat(config.credentialsProvider(properties)).isInstanceOf(DefaultCredentialsProvider.class);
	}
}
