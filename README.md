# Shipment service
Service for receiving request from external systems for creating and updating shipments.
Receives request from external systems for creating and updating shipments, 
and then sends the request to the appropriate internal services for processing. 
The service also provides an API for external systems to query the status of shipments.
##Techologies used for CI, CD
AWS GLue, AWS API Gateway, AWS ECS, AWS ECR, AWS Parameter Store, AWS RDS, AWS ElastiCache, AWS Prometheus, 
AWS CloudWatch, Git Actions, Git Packages, Docker, Confluent Cloud
##Techologies used for development
Spring Boot, Redis, Kafka, PostgreSQL, MapStruct, Avro
##Testing
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

curl command for creating api key:
```
curl -X POST "http://localhost:8080/api/apikey-policy" \
  -H "Content-Type: application/json" \
  -d '{
    "apiKey": "abc123xyz456",
    "clientId": 1001,
    "numberOfRequestsPerDay": 10000,
    "active": true
  }'
```