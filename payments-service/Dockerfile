FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY payments-service/mvnw .
COPY payments-service/.mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
COPY payments-service/pom.xml payments-service/pom.xml
RUN ./mvnw -f payments-service/pom.xml dependency:go-offline -B
COPY payments-service/src payments-service/src
RUN ./mvnw -f payments-service/pom.xml clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --chown=appuser:appgroup --from=builder /build/payments-service/target/*.jar payments-service.jar
USER appuser
ENTRYPOINT ["java", "-jar", "payments-service.jar"]