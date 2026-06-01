package org.example.repository;

import org.example.entity.RequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, UUID> {

    /**
     * Останні логи для конкретного ендпоінта (для UI деталей).
     */
    Page<RequestLog> findByEndpoint_IdOrderByTimestampDesc(UUID endpointId, Pageable pageable);

    /**
     * Кількість викликів ендпоінта.
     */
    long countByEndpoint_Id(UUID endpointId);

    /**
     * Видалити старі логи (для cleanup-задачі — наприклад, старші 7 днів).
     */
    @Modifying
    @Query("DELETE FROM RequestLog l WHERE l.timestamp < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);

    @Query("SELECT r FROM RequestLog r WHERE r.endpoint.user.id = :userId")
    List<RequestLog> findAllByUserId(@Param("userId") UUID userId);
}