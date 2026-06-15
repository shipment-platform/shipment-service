# Shipment service
Service for receiving request from external systems for creating, updating and deleting (canceling) shipments.
Receives request from external systems for creating and updating shipments, 
and then sends the request to the appropriate internal services for processing.

## Technologies 
Java 21, Spring Boot 3, PostgreSQL, Lombok, Mapstruct, Spring Kafka, Micrometer, Spring Redis, Protobuf, Testcontainers

## Implementation details
Outbox pattern used, async communication with Kafka, idempotency of messages and rating/burst limitation implemented
with Redis, in memory caching api keys with Caffeine. Model shared with Proto files located in maven dependency.
Metrics published using OTL scraper sidecar and published to AWS Prometheus -> AWS Grafana

## CI, CD, Cloud technologies
AWS API Gateway, AWS ECS, AWS ECR, AWS Parameter Store, AWS RDS (switched to Supabase), AWS ElastiCache 
(switched to Aiven), AWS CloudWatch, AWS Managed Prometheus, AWS Managed Grafana, Git Actions, Git Packages, Docker, 
Confluent Cloud Schema Registry, Confluent Cloud Kafka

##Testing

curl command for creating api key:
```
curl -X POST "http://api-gateway-host:8080/api/apikey-policy" \
  -H "Content-Type: application/json" \
  -d '{
    "apiKey": "abc123xyz456",
    "clientId": 1001,
    "numberOfRequestsPerDay": 10000,
    "active": true
  }'
```
Api key must be added manually also to AWS Api gateway and set rating policy
curl command for creating an shipment:

```
curl -X POST "http://localhost:8080/shipments" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: "abc123xyz456" \
  -d '{
    "idempotencyKey": "idem-123456",
    "externalId": "EXT-1001",
    "trackingNumber": "TRK-987654321",
    "orderId": "ORD-5555",
    "status": "CREATED",
    "carrier": "DHL",
    "shippingMethod": "EXPRESS",
    "recipientName": "John Doe",
    "recipientAddress": {
      "addressLine": "221B Baker Street",
      "city": "London",
      "country": "UK",
      "postalCode": "NW1 6XE",
      "state": "Greater London"
    },
    "recipientPhoneNumber": "+381641234567",
    "recipientEmail": "john.doe@example.com",
    "originName": "Warehouse Novi Sad",
    "originAddress": {
      "addressLine": "Bulevar Oslobodjenja 1",
      "city": "Novi Sad",
      "country": "Serbia",
      "postalCode": "21000",
      "state": "Vojvodina"
    },
    "originPhoneNumber": "+381641112223",
    "originEmail": "warehouse@example.com",
    "items": [
      {
        "name": "Laptop",
        "quantity": 1,
        "unit": "piece",
        "weight": 2.3
      },
      {
        "name": "Mouse",
        "quantity": 2,
        "unit": "piece",
        "weight": 0.2
      }
    ],
    "estimatedPickup": "2026-05-28T10:00:00Z",
    "estimatedDelivery": "2026-05-30T18:00:00Z",
    "createdAt": "2026-05-28T09:30:00Z"
  }'
```

