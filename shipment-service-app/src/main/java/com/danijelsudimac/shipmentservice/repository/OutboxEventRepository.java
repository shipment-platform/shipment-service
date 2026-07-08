package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import java.time.Instant;
import java.util.List;


public interface OutboxEventRepository  {
    List<OutboxEvent> lockNextBatch(int limit);
    void markPublished(Long id, Instant publishedAt);
    void markFailed(Long id);
    void markRetired(Long id, int retryCount);
    int deletePublishedOlderThan(Instant moment);
}
