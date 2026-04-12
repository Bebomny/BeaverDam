FROM eclipse-temurin:25-alpine AS builder
WORKDIR /builder

COPY app.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:25-alpine
WORKDIR /app

COPY --from=builder /builder/dependencies/ ./
COPY --from=builder /builder/spring-boot-loader/ ./
COPY --from=builder /builder/snapshot-dependencies/ ./
COPY --from=builder /builder/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]