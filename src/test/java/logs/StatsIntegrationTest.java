package logs;

import org.example.entity.MockEndpoint;
import org.example.entity.User;
import org.example.repository.MockEndpointRepository;
import org.example.repository.RequestLogRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = org.example.Main.class)
@AutoConfigureMockMvc
@ActiveProfiles("h2")
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
    private MockEndpoint testEndpoint;
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

        testEndpoint = MockEndpoint.builder()
                .user(testUser)
                .method("GET")
                .path("/api/test-data")
                .responseStatus(200)
                .responseBody("{\"message\": \"success\"}")
                .build();
        endpointRepository.save(testEndpoint);
    }

    @Test
    void shouldLogRequestAndReturnInStats() throws Exception {
        mockMvc.perform(get("/mock/" + USER_HASH + "/api/test-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success")); // Перевіряємо, що мок віддав правильне body

        Thread.sleep(500);

        org.example.security.UserPrincipal myPrincipal = new org.example.security.UserPrincipal(testUser); // Або як він у тебе створюється

        mockMvc.perform(get("/api/stats/me")
                        // Використовуємо .authentication() замість .user()
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        myPrincipal, null, myPrincipal.getAuthorities()
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].path").value("/api/test-data"))
                .andExpect(jsonPath("$[0].status").value(200));
    }
}