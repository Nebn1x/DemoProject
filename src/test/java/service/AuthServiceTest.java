package service;

import org.example.dto.auth.AuthResponse;
import org.example.dto.auth.LoginRequest;
import org.example.dto.auth.RegisterRequest;
import org.example.entity.User;
import org.example.exception.EmailAlreadyExistsException;
import org.example.repository.UserRepository;
import org.example.security.JwtService;
import org.example.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@test.com", "password123");
        loginRequest = new LoginRequest("test@test.com", "password123");
    }

    @Test
    @DisplayName("реєстрація створює користувача і повертає токен")
    void register_success() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.existsByUserHash(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("jwt_token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        // save повертає того ж юзера з проставленим id
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.token()).isEqualTo("jwt_token");
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.userHash()).hasSize(8);

        // пароль має бути захешований, а не збережений у відкритому вигляді
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed_password");
        assertThat(userCaptor.getValue().getPasswordHash()).isNotEqualTo("password123");
    }

    @Test
    @DisplayName("реєстрація з існуючим email кидає виняток")
    void register_emailExists_throws() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("test@test.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("логін з вірними даними повертає токен")
    void login_success() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .passwordHash("hashed")
                .userHash("aB3xK9p2")
                .role("USER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // успішна автентифікація не кидає виняток
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("jwt_token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.token()).isEqualTo("jwt_token");
        assertThat(response.userHash()).isEqualTo("aB3xK9p2");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("логін з невірним паролем кидає BadCredentialsException")
    void login_wrongPassword_throws() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), anyString(), anyString());
    }
}