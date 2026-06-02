package org.example.logs;

import org.example.entity.MockEndpoint;
import org.example.entity.User;
import org.example.repository.MockEndpointRepository;
import org.example.repository.RequestLogRepository;
import org.example.repository.UserRepository;
import org.example.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = org.example.Main.class)
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "app.rate-limit.enabled=false",       // вимикаємо rate-limit (не лізе в Redis)
        "spring.cache.type=none",             // вимикаємо кеш повністю (не лізе в Redis)
        "spring.data.redis.repositories.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
@DisplayName("Stats (інтеграційний)")
class StatsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockEndpointRepository endpointRepository;

    @Autowired
    private RequestLogRepository requestLogRepository;

    private User testUser;
    private final String USER_HASH = "testerHash12";

    @BeforeEach
    void setUp() {
        requestLogRepository.deleteAll();
        endpointRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUserHash(USER_HASH);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("fakePassword");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);

        MockEndpoint testEndpoint = MockEndpoint.builder()
                .user(testUser)
                .method("GET")
                .path("/api/test-data")
                .responseStatus(200)
                .responseBody("{\"message\": \"success\"}")
                .build();
        endpointRepository.save(testEndpoint);
    }

    @Test
    @DisplayName("виклик mock логується і з'являється в stats")
    void shouldLogRequestAndReturnInStats() throws Exception {
        mockMvc.perform(get("/mock/" + USER_HASH + "/api/test-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"));

        Thread.sleep(800);

        UserPrincipal principal = new UserPrincipal(testUser);

        mockMvc.perform(get("/api/v1/stats/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].path").value("/api/test-data"))
                .andExpect(jsonPath("$[0].status").value(200));
    }
}