package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostgresOutboxEventRepository extends JpaRepository<OutboxEvent, Long>, OutboxEventRepository {

    @Query(value = """
            SELECT *
            FROM outbox_event
            WHERE published = false
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(@Param("limit") int limit);
}