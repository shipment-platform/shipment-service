package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository {
    List<OutboxEvent> lockNextBatch(@Param("limit") int limit);
    OutboxEvent save(OutboxEvent entity);
}
