# Railway / Docker: pin Java 8 to match build.gradle (local JDK unchanged).
# Build stage
FROM eclipse-temurin:8-jdk-jammy AS build
WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew \
    && ./gradlew bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:8-jre-jammy
WORKDIR /app

RUN useradd --system --create-home --home-dir /app appuser \
    && chown -R appuser:appuser /app
USER appuser

COPY --from=build --chown=appuser:appuser /app/build/libs/*.jar app.jar

# Railway sets PORT at runtime; default for local docker run
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -Djava.security.egd=file:/dev/./urandom -jar app.jar --server.port=${PORT}"]
