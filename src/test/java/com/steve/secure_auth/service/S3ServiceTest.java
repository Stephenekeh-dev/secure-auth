package com.steve.secure_auth.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3Service Tests")
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", "secure-auth-profile-images");
        ReflectionTestUtils.setField(s3Service, "region", "us-east-1");
    }


    // -------uploadFile-------
    @Test
    @DisplayName("uploadFile — calls s3Client.putObject once")
    void uploadFile_callsPutObjectOnce() {
        MockMultipartFile file = new MockMultipartFile(
                "profileImage",
                "photo.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        s3Service.uploadFile(file);

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("uploadFile — returns URL containing bucket name and region")
    void uploadFile_returnsCorrectUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "profileImage",
                "photo.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        String url = s3Service.uploadFile(file);

        assertThat(url).contains("secure-auth-profile-images");
        assertThat(url).contains("us-east-1");
        assertThat(url).startsWith("https://");
    }

    @Test
    @DisplayName("uploadFile — returned URL contains profile-images path")
    void uploadFile_urlContainsProfileImagesPath() {
        MockMultipartFile file = new MockMultipartFile(
                "profileImage",
                "photo.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        String url = s3Service.uploadFile(file);

        assertThat(url).contains("profile-images/");
    }

    @Test
    @DisplayName("uploadFile — returned URL contains original filename")
    void uploadFile_urlContainsOriginalFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "profileImage",
                "photo.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        String url = s3Service.uploadFile(file);

        assertThat(url).contains("photo.jpg");
    }

    @Test
    @DisplayName("uploadFile — throws RuntimeException when S3 throws")
    void uploadFile_throwsWhenS3Throws() {
        MockMultipartFile file = new MockMultipartFile(
                "profileImage",
                "photo.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        doThrow(new RuntimeException("S3 error"))
                .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThatThrownBy(() -> s3Service.uploadFile(file))
                .isInstanceOf(RuntimeException.class);
    }


    // deleteFile

    @Test
    @DisplayName("deleteFile — calls s3Client.deleteObject once")
    void deleteFile_callsDeleteObjectOnce() {
        String fileUrl = "https://secure-auth-profile-images.s3.us-east-1.amazonaws.com/" +
                "profile-images/uuid-photo.jpg";

        s3Service.deleteFile(fileUrl);

        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("deleteFile — extracts correct key from URL")
    void deleteFile_extractsCorrectKey() {
        String fileUrl = "https://secure-auth-profile-images.s3.us-east-1.amazonaws.com/" +
                "profile-images/uuid-photo.jpg";

        s3Service.deleteFile(fileUrl);

        verify(s3Client).deleteObject(argThat((DeleteObjectRequest req) ->
                req.key().equals("profile-images/uuid-photo.jpg") &&
                        req.bucket().equals("secure-auth-profile-images")
        ));
    }
}