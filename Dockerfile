# [Stage 1] 빌드 단계
FROM public.ecr.aws/docker/library/eclipse-temurin:25-jdk AS builder

WORKDIR /app

# 1. Gradle 래퍼 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 2. 모든 모듈 소스 복사 (구조적 안정성을 위해 전체 복사 권장)
COPY . .

# 3. 빌드 실행 (MODULE_NAME을 인자로 받음)
ARG MODULE_NAME
RUN chmod +x ./gradlew
# clean 제거: 일부 모듈 빌드 시 의존성(common) 결과물이 삭제될 가능성 방지 및 캐싱 활용
# Run Gradle with verbose flags to surface detailed errors in Docker logs
RUN ./gradlew :${MODULE_NAME}:bootJar -x test --no-daemon --stacktrace --warning-mode all

# plain JAR 제거 (COPY 시 모호성 방지)
RUN rm -f ${MODULE_NAME}/build/libs/*-plain.jar

# [Stage 2] 실행 단계
FROM public.ecr.aws/docker/library/eclipse-temurin:25-jre

WORKDIR /app

ARG MODULE_NAME

# 4. 빌드된 JAR 파일 복사
COPY --from=builder /app/${MODULE_NAME}/build/libs/*.jar app.jar

# 5. 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
