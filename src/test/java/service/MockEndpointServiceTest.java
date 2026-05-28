package service;

import org.example.dto.endpoint.CreateEndpointRequest;
import org.example.dto.endpoint.EndpointResponse;
import org.example.dto.endpoint.UpdateEndpointRequest;
import org.example.entity.MockEndpoint;
import org.example.entity.User;
import org.example.exception.EndpointAlreadyExistsException;
import org.example.exception.EndpointNotFoundException;
import org.example.repository.MockEndpointRepository;
import org.example.repository.UserRepository;
import org.example.service.MockEndpointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MockEndpointService")
class MockEndpointServiceTest {

    @Mock private MockEndpointRepository endpointRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private MockEndpointService endpointService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        // base-url зазвичай інжектиться через @Value - ставимо вручну для тесту
        ReflectionTestUtils.setField(endpointService, "baseUrl", "http://localhost:8080");

        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@test.com")
                .userHash("aB3xK9p2")
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("створення ендпоінта повертає правильний fullUrl")
    void create_success() {
        CreateEndpointRequest req = new CreateEndpointRequest(
                "GET", "/api/users", "{\"ok\":true}", 200, "application/json", 0, null
        );

        when(endpointRepository.existsByUser_IdAndMethodAndPath(userId, "GET", "/api/users"))
                .thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(endpointRepository.save(any(MockEndpoint.class))).thenAnswer(inv -> {
            MockEndpoint e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EndpointResponse response = endpointService.create(userId, req);

        assertThat(response.method()).isEqualTo("GET");
        assertThat(response.path()).isEqualTo("/api/users");
        assertThat(response.fullUrl()).isEqualTo("http://localhost:8080/mock/aB3xK9p2/api/users");
        assertThat(response.responseStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("створення дубліката кидає виняток")
    void create_duplicate_throws() {
        CreateEndpointRequest req = new CreateEndpointRequest(
                "GET", "/api/users", "{}", 200, "application/json", 0, null
        );

        when(endpointRepository.existsByUser_IdAndMethodAndPath(userId, "GET", "/api/users"))
                .thenReturn(true);

        assertThatThrownBy(() -> endpointService.create(userId, req))
                .isInstanceOf(EndpointAlreadyExistsException.class);

        verify(endpointRepository, never()).save(any());
    }

    @Test
    @DisplayName("отримання чужого ендпоінта кидає NotFound")
    void getById_notOwned_throws() {
        UUID endpointId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User otherUser = User.builder().id(otherUserId).userHash("xxxxxxxx").build();

        MockEndpoint foreignEndpoint = MockEndpoint.builder()
                .id(endpointId)
                .user(otherUser)   // належить ІНШОМУ користувачу
                .method("GET")
                .path("/secret")
                .build();

        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(foreignEndpoint));

        assertThatThrownBy(() -> endpointService.getById(userId, endpointId))
                .isInstanceOf(EndpointNotFoundException.class);
    }

    @Test
    @DisplayName("оновлення змінює лише передані поля")
    void update_partialFields() {
        UUID endpointId = UUID.randomUUID();
        MockEndpoint existing = MockEndpoint.builder()
                .id(endpointId)
                .user(user)
                .method("GET")
                .path("/api/users")
                .responseStatus(200)
                .delayMs(0)
                .contentType("application/json")
                .build();

        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(existing));
        when(endpointRepository.save(any(MockEndpoint.class))).thenAnswer(inv -> inv.getArgument(0));

        // оновлюємо лише статус і затримку
        UpdateEndpointRequest req = new UpdateEndpointRequest(
                null, null, null, 404, null, 500
        );

        EndpointResponse response = endpointService.update(userId, endpointId, req);

        assertThat(response.responseStatus()).isEqualTo(404);  // змінилось
        assertThat(response.delayMs()).isEqualTo(500);          // змінилось
        assertThat(response.method()).isEqualTo("GET");         // лишилось
        assertThat(response.path()).isEqualTo("/api/users");    // лишилось
    }

    @Test
    @DisplayName("видалення неіснуючого кидає NotFound")
    void delete_notFound_throws() {
        UUID endpointId = UUID.randomUUID();
        when(endpointRepository.findById(endpointId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> endpointService.delete(userId, endpointId))
                .isInstanceOf(EndpointNotFoundException.class);

        verify(endpointRepository, never()).delete(any());
    }
}