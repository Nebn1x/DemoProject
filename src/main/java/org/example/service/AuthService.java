package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.auth.AuthResponse;
import org.example.dto.auth.LoginRequest;
import org.example.dto.auth.RegisterRequest;
import org.example.entity.User;
import org.example.exception.EmailAlreadyExistsException;
import org.example.repository.UserRepository;
import org.example.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String HASH_ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int HASH_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .userHash(generateUniqueUserHash())
                .role("USER")
                .build();

        user = userRepository.save(user);
        log.info("Зареєстровано нового користувача: {} (hash={})", user.getEmail(), user.getUserHash());

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getUserHash());
        return AuthResponse.of(token, jwtService.getExpirationMs(),
                user.getId(), user.getEmail(), user.getUserHash());
    }

    public AuthResponse login(LoginRequest request) {
        // Кидає BadCredentialsException, якщо пароль невірний - перехопить GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(); // після успішної автентифікації юзер точно існує

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getUserHash());
        log.info("Користувач увійшов: {}", user.getEmail());

        return AuthResponse.of(token, jwtService.getExpirationMs(),
                user.getId(), user.getEmail(), user.getUserHash());
    }

    /**
     * Генерує унікальний короткий хеш для URL (типу "aB3xK9p2").
     */
    private String generateUniqueUserHash() {
        String hash;
        do {
            StringBuilder sb = new StringBuilder(HASH_LENGTH);
            for (int i = 0; i < HASH_LENGTH; i++) {
                sb.append(HASH_ALPHABET.charAt(RANDOM.nextInt(HASH_ALPHABET.length())));
            }
            hash = sb.toString();
        } while (userRepository.existsByUserHash(hash));
        return hash;
    }
}