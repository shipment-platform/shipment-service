package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostgresqlOutboxEventJPARepository extends OutboxEventJPARepository, JpaRepository<OutboxEvent, Integer> {
}
