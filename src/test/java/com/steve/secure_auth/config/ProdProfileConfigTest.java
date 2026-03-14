package com.steve.secure_auth.config;



import com.steve.secure_auth.SecureAuthApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SecureAuthApplication.class)
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/secure_auth",
        "spring.datasource.username=postgres",
        "spring.datasource.password=testpassword",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "jwt.secret=v2DQ/qdsoALW2JjL8a86ENcoBIE1QWAm2sIn10LJa1U=",
        "jwt.expiration-ms=3600000",
        "spring.mail.username=test@gmail.com",
        "spring.mail.password=testpassword",
        "app.base-url=http://localhost:8080",
        "aws.s3.bucket-name=test-bucket",
        "aws.s3.region=us-east-1",
        "aws.s3.access-key=testkey",
        "aws.s3.secret-key=testsecret",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
@DisplayName("Production Profile Configuration Tests")
class ProdProfileConfigTest {

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("prod profile is active")
    void prodProfile_isActive() {

        assertThat(environment.getActiveProfiles()).contains("prod");
    }

    @Test
    @DisplayName("jwt.secret property is loaded")
    void jwtSecret_isLoaded() {

        assertThat(environment.getProperty("jwt.secret")).isNotNull();
    }

    @Test
    @DisplayName("jwt.expiration-ms property is loaded")
    void jwtExpirationMs_isLoaded() {

        assertThat(environment.getProperty("jwt.expiration-ms")).isEqualTo("3600000");
    }

    @Test
    @DisplayName("app.base-url property is loaded")
    void appBaseUrl_isLoaded() {

        assertThat(environment.getProperty("app.base-url")).isNotNull();
    }

    @Test
    @DisplayName("aws.s3.bucket-name property is loaded")
    void s3BucketName_isLoaded() {

        assertThat(environment.getProperty("aws.s3.bucket-name")).isNotNull();
    }

    @Test
    @DisplayName("aws.s3.region property is loaded")
    void s3Region_isLoaded() {

        assertThat(environment.getProperty("aws.s3.region")).isNotNull();
    }
}
