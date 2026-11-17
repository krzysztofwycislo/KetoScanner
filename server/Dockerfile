FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
COPY shared ./shared
COPY server ./server
RUN chmod +x gradlew
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon --console=plain -p server installDist

FROM eclipse-temurin:21-jre
RUN groupadd --system carbpeek \
    && useradd --system --gid carbpeek --no-create-home --shell /usr/sbin/nologin carbpeek
COPY --from=build /workspace/server/build/install/carbpeek-server /app
ARG SERVER_VERSION=dev
ENV SERVER_VERSION=${SERVER_VERSION}
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
USER carbpeek
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -fsS http://localhost:8080/health || exit 1
ENTRYPOINT ["/app/bin/carbpeek-server"]
