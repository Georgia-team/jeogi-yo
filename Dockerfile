FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew gradlew.bat ./
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
