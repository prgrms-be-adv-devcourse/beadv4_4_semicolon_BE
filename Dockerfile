FROM openjdk:25-jdk-slim

# JAR 파일 위치
ARG JAR_FILE=build/libs/*.jar

# JAR 파일 복사
COPY ${JAR_FILE} app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]