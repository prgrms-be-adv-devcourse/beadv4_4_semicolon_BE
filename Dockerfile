# [Stage 1] 빌드 단계
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# 1. Gradle 래퍼 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 2. 멀티 모듈 소스 복사
COPY common common
COPY semicolon semicolon

# 3. 빌드 실행 (:semicolon:bootJar)
RUN chmod +x ./gradlew
RUN ./gradlew clean :semicolon:bootJar -x test

# [Stage 2] 실행 단계
FROM eclipse-temurin:25-jdk

WORKDIR /app

# 4. 빌드된 JAR 파일 복사 (/app/app.jar 위치에 생성됨)
COPY --from=builder /app/semicolon/build/libs/*.jar app.jar

# 5. 실행 명령어 수정 (경로 일치시킴)
ENTRYPOINT ["java", "-jar", "app.jar"]