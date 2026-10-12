package com.obj.nc.config;

import com.obj.nc.security.config.CorsConfigProperties;
import com.obj.nc.security.config.DisabledSecurityConfig;
import com.obj.nc.security.config.JwtAuthenticationEntryPoint;
import com.obj.nc.security.config.JwtRequestFilter;
import com.obj.nc.security.config.JwtSecurityConfig;
import com.obj.nc.security.config.JwtTokenUtil;
import com.obj.nc.security.config.NcJwtConfigProperties;
import com.obj.nc.security.config.SecurityUtilsConfig;
import com.obj.nc.security.config.WebSecurityConfig;
import com.obj.nc.security.service.JwtUserDetailsService;
import com.obj.nc.utils.JsonUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TrailingSlashCompatibilityTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(
            "notiflow-test-signing-secret-for-hs512-at-least-64-bytes-long-1234567890"
                    .getBytes(StandardCharsets.UTF_8));

    static Stream<Arguments> configurations() {
        return Stream.of(
                Arguments.of(false, ""),
                Arguments.of(false, "/notiflow"),
                Arguments.of(true, ""),
                Arguments.of(true, "/notiflow"));
    }

    @ParameterizedTest(name = "jwt={0}, contextPath={1}")
    @MethodSource("configurations")
    void preservesRoutingAndSecurityWithAndWithoutTrailingSlashes(boolean jwtEnabled, String contextPath)
            throws Exception {
        // A real servlet container exercises filter registration and ordering, including context paths.
        try (var context = SpringApplication.run(TestApplication.class,
                "--server.port=0",
                "--server.address=127.0.0.1",
                "--server.servlet.context-path=" + contextPath,
                "--nc.jwt.enabled=" + jwtEnabled,
                "--nc.jwt.username=testUser",
                "--nc.jwt.password=testPassword",
                "--nc.jwt.signature-secret=" + SECRET,
                "--spring.jmx.enabled=false");
             var client = HttpClient.newBuilder()
                     .followRedirects(HttpClient.Redirect.NEVER)
                     .connectTimeout(Duration.ofSeconds(5))
                     .build()) {
            int port = context.getEnvironment().getRequiredProperty("local.server.port", Integer.class);
            URI base = URI.create("http://127.0.0.1:" + port + contextPath + "/");
            String token = jwtEnabled ? context.getBean(JwtTokenUtil.class).generateToken(
                    User.withUsername("testUser").password("unused").roles("USER").build(), SECRET) : null;
            String payload = "{\"text\":\"Ahoj / čau\"}";

            for (String suffix : new String[]{"", "/"}) {
                var get = request(client, base.resolve("events" + suffix + "?marker=keep-me"), token, null);
                assertThat(get.statusCode()).as("GET /events%s", suffix).isEqualTo(200);
                assertThat(JsonUtils.readJsonNodeFromJSONString(get.body()).path("marker").asText())
                        .isEqualTo("keep-me");

                var post = request(client, base.resolve("events" + suffix), token, payload);
                assertThat(post.statusCode()).as("POST /events%s", suffix).isEqualTo(200);
                assertThat(post.headers().allValues("Location")).isEmpty();
                assertThat(post.body()).isEqualTo(payload);

                if (jwtEnabled) {
                    var missingToken = request(client, base.resolve("events" + suffix), null, payload);
                    assertThat(missingToken.statusCode()).as("missing JWT, POST /events%s", suffix)
                            .isEqualTo(401);
                    var invalidToken = request(client, base.resolve("events" + suffix), "invalid", payload);
                    assertThat(invalidToken.statusCode()).as("invalid JWT, POST /events%s", suffix)
                            .isEqualTo(401);
                }

                // This public path must be normalized before both JWT filtering and authorization.
                var publicGet = request(client,
                        base.resolve("delivery-info/messages/123/mark-as-read" + suffix), null, null);
                assertThat(publicGet.statusCode()).as("public tracking URL with suffix '%s'", suffix)
                        .isEqualTo(200);
            }
        }
    }

    private HttpResponse<String> request(HttpClient client, URI uri, String token, String body) throws Exception {
        var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            request.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
    @Import({SpringMvcConfig.class, WebSecurityConfig.class, JwtSecurityConfig.class,
            DisabledSecurityConfig.class, JwtRequestFilter.class, JwtAuthenticationEntryPoint.class,
            JwtTokenUtil.class, NcJwtConfigProperties.class, CorsConfigProperties.class,
            SecurityUtilsConfig.class, JwtUserDetailsService.class, TestEndpoints.class})
    static class TestApplication {
    }

    @TestComponent
    @RestController
    static class TestEndpoints {
        @GetMapping("/events")
        Map<String, String> events(@RequestParam String marker) {
            return Map.of("marker", marker);
        }

        @PostMapping(value = "/events", consumes = "application/json", produces = "application/json")
        String receiveEvent(@RequestBody String body) {
            return body;
        }

        @GetMapping("/delivery-info/messages/{messageId}/mark-as-read")
        Map<String, String> trackingPixel() {
            return Map.of("status", "read");
        }
    }
}
