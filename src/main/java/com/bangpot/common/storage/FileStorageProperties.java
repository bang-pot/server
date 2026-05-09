package com.bangpot.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "bangpot.storage")
public class FileStorageProperties {

	private String publicBaseUrl = "";

	private S3 s3 = new S3();

	@Getter
	@Setter
	public static class S3 {

		private String bucket = "";

		private String region = "ap-northeast-2";

		private String accessKey = "";

		private String secretKey = "";
	}
}
