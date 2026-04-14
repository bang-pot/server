package com.bangpot.common.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "bangpot.storage")
public class FileStorageProperties {

	private String rootDirectory = Paths.get(System.getProperty("user.dir"), "storage").toString();

	public Path rootDirectoryPath() {
		return Paths.get(rootDirectory).toAbsolutePath().normalize();
	}
}
