package org.example.repository;

import org.example.entity.MockEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MockEndpointRepository extends JpaRepository<MockEndpoint, UUID> {

    /**
     * Пошук схеми для catch-all контролера: за хешем користувача, методом і шляхом.
     */
    @Query("""
        SELECT e FROM MockEndpoint e
        WHERE e.user.userHash = :userHash
          AND e.method = :method
          AND e.path = :path
        """)
    Optional<MockEndpoint> findByUserHashAndMethodAndPath(
            @Param("userHash") String userHash,
            @Param("method") String method,
            @Param("path") String path
    );

    /**
     * Список ендпоінтів конкретного користувача (для дашборда) з пагінацією.
     */
    Page<MockEndpoint> findByUser_Id(UUID userId, Pageable pageable);

    /**
     * Кількість ендпоінтів користувача (для лімітів).
     */
    long countByUser_Id(UUID userId);

    /**
     * Видалити прострочені ендпоінти (для @Scheduled задачі очистки).
     */
    @Modifying
    @Query("DELETE FROM MockEndpoint e WHERE e.expiresAt IS NOT NULL AND e.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    boolean existsByUser_IdAndMethodAndPath(UUID userId, String method, String path);
}