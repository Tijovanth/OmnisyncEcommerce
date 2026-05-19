FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY eureka-server/mvnw .
COPY eureka-server/.mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
COPY eureka-server/pom.xml eureka-server/pom.xml
RUN ./mvnw -f eureka-server/pom.xml dependency:go-offline -B
COPY eureka-server/src eureka-server/src
RUN ./mvnw -f eureka-server/pom.xml clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --chown=appuser:appgroup --from=builder /build/eureka-server/target/*.jar eureka-server.jar
USER appuser
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]