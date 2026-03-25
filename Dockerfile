FROM eclipse-temurin:25-alpine

#RUN addgroup -S beaverdam && adduser -S beaverdam -G beaverdam
#USER beaverdam:beaverdam
WORKDIR /app
COPY app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]