FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/shipment-service-app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]