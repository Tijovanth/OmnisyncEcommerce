FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY api_gateway/mvnw .
COPY api_gateway/.mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
COPY api_gateway/pom.xml api_gateway/pom.xml
RUN ./mvnw -f api_gateway/pom.xml dependency:go-offline -B
COPY api_gateway/src api_gateway/src
RUN ./mvnw -f api_gateway/pom.xml clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --chown=appuser:appgroup --from=builder /build/api_gateway/target/*.jar api_gateway.jar
USER appuser
ENTRYPOINT ["java", "-jar", "api_gateway.jar"]