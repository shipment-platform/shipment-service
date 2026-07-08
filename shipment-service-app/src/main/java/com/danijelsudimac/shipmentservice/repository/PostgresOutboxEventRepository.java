package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.entity.OutboxEventStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class PostgresOutboxEventRepository implements  OutboxEventRepository {

    private final JdbcTemplate jdbc;

    public PostgresOutboxEventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OutboxEvent> lockNextBatch(int limit) {

        return jdbc.query("""
            UPDATE outbox_event
            SET status = 'PROCESSING'
            WHERE id IN (
                SELECT id
                FROM outbox_event
                WHERE status = 'PENDING'
                ORDER BY created_at
                LIMIT ?
                FOR UPDATE SKIP LOCKED
            )
            RETURNING *
            """,
                new BeanPropertyRowMapper<>(OutboxEvent.class),
                limit
        );
    }

    public void markPublished(Long id, Instant publishedAt) {

        jdbc.update("""
            UPDATE outbox_event
            SET
                status = ?,
                published_at = ?
            WHERE id = ?
            """,
                OutboxEventStatus.PUBLISHED.name(),
                Timestamp.from(publishedAt),
                id
        );
    }

    public void markFailed(Long id) {

        jdbc.update("""
            UPDATE outbox_event
            SET
                status = ?
            WHERE id = ?
            """,
                OutboxEventStatus.FAILED.name(),
                id
        );
    }

    public void markRetired(Long id, int retryCount) {

        jdbc.update("""
            UPDATE outbox_event
            SET
                status = ?,
                retry_count = ?
            WHERE id = ?
            """,
                OutboxEventStatus.PENDING.name(),
                retryCount,
                id
        );
    }

    public int deletePublishedOlderThan(Instant moment) {
        return jdbc.update("""
            DELETE FROM outbox_event
            WHERE status = ?
              AND published_at < ?
            """,
                OutboxEventStatus.PUBLISHED.name(),
                Timestamp.from(moment)
        );
    }
}
