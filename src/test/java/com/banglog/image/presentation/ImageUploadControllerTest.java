package com.banglog.image.presentation;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.banglog.common.error.ApiErrorResponseFactory;
import com.banglog.common.error.GlobalApiExceptionHandler;
import com.banglog.image.application.usecase.UploadTemporaryImageUseCase;
import com.banglog.image.domain.ImageUploadCategory;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ImageUploadController.class)
@Import({GlobalApiExceptionHandler.class, ApiErrorResponseFactory.class})
class ImageUploadControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UploadTemporaryImageUseCase uploadTemporaryImageUseCase;

	@Test
	void uploadsLogPhotoForAuthenticatedUser() throws Exception {
		assertUploadEndpoint(
			"/api/uploads/log-photos",
			ImageUploadCategory.MEETING_LOG_PHOTO,
			"https://banglog-image.s3.ap-northeast-2.amazonaws.com/temp/log-photos/stored-sample.jpg"
		);
	}

	@Test
	void uploadsProfileImageForAuthenticatedUser() throws Exception {
		assertUploadEndpoint(
			"/api/uploads/profile-images",
			ImageUploadCategory.PROFILE_IMAGE,
			"https://banglog-image.s3.ap-northeast-2.amazonaws.com/temp/profile-images/stored-sample.jpg"
		);
	}

	@Test
	void uploadsCrewCoverImageForAuthenticatedUser() throws Exception {
		assertUploadEndpoint(
			"/api/uploads/crew-cover-images",
			ImageUploadCategory.CREW_COVER_IMAGE,
			"https://banglog-image.s3.ap-northeast-2.amazonaws.com/temp/crew-cover-images/stored-sample.jpg"
		);
	}

	@Test
	void returnsUnauthorizedWhenUploadWithoutAuthentication() throws Exception {
		var file = new MockMultipartFile("file", "sample.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

		mockMvc.perform(
			multipart("/api/uploads/log-photos")
				.file(file)
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
	}

	private void assertUploadEndpoint(
		String path,
		ImageUploadCategory category,
		String expectedUrl
	) throws Exception {
		var file = new MockMultipartFile("file", "sample.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

		when(uploadTemporaryImageUseCase.handle(argThat(command ->
			command.userId().equals(7L)
				&& command.category() == category
				&& command.file().equals(file)
		)))
			.thenReturn(UploadTemporaryImageUseCase.Result.of(
				123L,
				expectedUrl,
				file.getSize()
			));

		mockMvc.perform(
			multipart(path)
				.file(file)
				.principal(new UsernamePasswordAuthenticationToken(7L, null))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.uploadId").value(123))
			.andExpect(jsonPath("$.url").value(expectedUrl))
			.andExpect(jsonPath("$.sizeBytes").value((int) file.getSize()));
	}
}
