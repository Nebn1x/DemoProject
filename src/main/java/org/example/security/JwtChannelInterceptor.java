package org.example.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final UserRepository userRepository; // додати

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Спробуємо отримати токен з різних місць
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null) {
                authHeader = accessor.getFirstNativeHeader("authorization");
            }
            if (authHeader == null) {
                authHeader = accessor.getFirstNativeHeader("token");
            }

            log.info("Auth header: {}", authHeader); // для дебагу

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtService.isTokenValid(token)) {
                    var userId = jwtService.extractUserId(token);
                    var authorities = getUserAuthorities(userId);

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            authorities
                    );

                    accessor.setUser(authentication);
                    log.info("User authenticated: {}", userId); // для дебагу
                } else {
                    log.warn("Invalid token");
                }
            } else {
                log.warn("No Authorization header found");
            }
        }
        return message;
    }

    private Collection<? extends GrantedAuthority> getUserAuthorities(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
                .orElse(List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}