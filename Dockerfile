FROM eclipse-temurin:25-jdk

COPY build/libs/*-boot.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
