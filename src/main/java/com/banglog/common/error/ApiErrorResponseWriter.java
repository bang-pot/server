package com.banglog.common.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiErrorResponseWriter {

	private final ObjectMapper objectMapper;
	private final ApiErrorResponseFactory apiErrorResponseFactory;

	public void write(HttpServletResponse response, ApiErrorCode errorCode) throws IOException {
		write(response, errorCode, errorCode.message(), List.of());
	}

	public void write(HttpServletResponse response, ApiErrorCode errorCode, String message) throws IOException {
		write(response, errorCode, message, List.of());
	}

	public void write(
		HttpServletResponse response,
		ApiErrorCode errorCode,
		String message,
		List<ApiErrorField> fieldErrors
	) throws IOException {
		response.setStatus(errorCode.status().value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(
			response.getOutputStream(),
			apiErrorResponseFactory.create(errorCode, message, fieldErrors)
		);
	}
}
