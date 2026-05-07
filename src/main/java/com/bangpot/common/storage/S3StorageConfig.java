package com.bangpot.common.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class S3StorageConfig {

	@Bean
	S3Client s3Client(FileStorageProperties fileStorageProperties) {
		return S3Client.builder()
			.region(Region.of(fileStorageProperties.getS3().getRegion()))
			.credentialsProvider(credentialsProvider(fileStorageProperties))
			.build();
	}

	AwsCredentialsProvider credentialsProvider(FileStorageProperties fileStorageProperties) {
		String accessKey = fileStorageProperties.getS3().getAccessKey();
		String secretKey = fileStorageProperties.getS3().getSecretKey();
		if (isBlank(accessKey) || isBlank(secretKey)) {
			return DefaultCredentialsProvider.builder().build();
		}
		return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
