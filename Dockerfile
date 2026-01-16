FROM eclipse-temurin:25-jre
WORKDIR /app
COPY semicolon/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
