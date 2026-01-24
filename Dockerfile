# [Stage 1] 빌드 단계 (Builder)
# Java 25 JDK 이미지를 사용하여 소스를 빌드합니다.
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Gradle 캐시 효율화를 위해 설정 파일만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 권한 부여 및 빌드 (테스트 생략)
# 이 명령어가 실행되면 Docker 안에서 /app/build/libs/ 에 JAR가 생성됩니다.
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test

# [Stage 2] 실행 단계 (Runner)
# 실행에 필요한 가벼운 이미지만 가져옵니다.
FROM eclipse-temurin:25-jdk

WORKDIR /app

# Stage 1(builder)에서 만들어진 JAR 파일만 쏙 가져옵니다.
# 경로 오차 없이 확실하게 복사됩니다.
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]