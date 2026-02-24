# 프로젝트 운영 환경 안정화 및 배포 종합 인수인계 문서 (Handover)

본 문서는 운영 서버(EC2/k3s, 8Gi RAM) 환경의 안정성을 확보하고, 배포 장애를 해결하기 위해 진행된 모든 인프라 및 애플리케이션 수준의 조치 사항을 기록합니다.

---

## 1. CI/CD 파이프라인 및 배포 프로세스 개선

### 빌드 환경 안정화
- **베이스 이미지 레지스트리 교체**: Docker Hub의 전송 제한(429) 및 GHCR의 인증 에러(403)를 해결하기 위해 모든 Dockerfile의 베이스 이미지를 **Amazon ECR Public(public.ecr.aws)**으로 전면 교체했습니다.
- **캐싱 최적화**: GitHub Actions의 `type=gha` 캐싱 설정을 강화하여 빌드 속도를 개선했습니다.

### 배포 워크플로우 (deploy.yml) 고도화
- **순차 배포 로직 도입**: 서버 자원(RAM) 고갈을 방지하기 위해 9개 서비스를 하나씩 빌드/배포하는 순차 방식을 적용했습니다.
- **Scale 0 -> 1 전략**: 기존 Pod을 완전히 종료(scale 0)한 후 새 버전을 올리는 방식을 채택하여 라이브 중 메모리 중첩 부하를 원천 차단했습니다.
- **타임아웃 연장**: 순차 배포 시간을 고려하여 `command_timeout`을 **30m**으로, 롤아웃 대기 시간을 **10m**으로 연장했습니다.
- **AI 전용 워크플로우 생성**: 특정 기능(AI 등)만 빠른 패치가 가능하도록 [deploy-ai.yml](file:///c:/Users/pc/IdeaProjects/beadv4_4_semicolon_BE/.github/workflows/deploy-ai.yml)을 신규 생성했습니다.

---

## 2. 서버 자원 최적화 (Memory Management)

8Gi RAM 서버에서 9개 마이크로서비스와 인프라(DB, ES, Kafka)를 안정적으로 운영하기 위해 **3단계 메모리 등급제**를 도입했습니다.

| 등급 | 대상 서비스 | JVM Heap (-Xmx) | K8s Limit | K8s Request |
| :--- | :--- | :--- | :--- | :--- |
| **Light** | auth, user, order, coupon, payment, deposit | 256m | 448Mi | 256Mi |
| **Medium** | product, settlement | 384m | 640Mi | 384Mi |
| **Heavy** | ai | 512m | 832Mi | 512Mi |

- **적용**: [deploy.yml](file:///c:/Users/pc/IdeaProjects/beadv4_4_semicolon_BE/.github/workflows/deploy.yml)에서 `JAVA_TOOL_OPTIONS` 환경변수를 통해 런타임에 주입하도록 통일했습니다.
- **상태**: 현재 위 설정 적용 후 서비스들이 Out-Of-Memory(OOM) 없이 안정적인 구동 상태를 유지하고 있습니다.

---

## 3. 인프라 및 공통 설정 리팩토링

### Kubernetes 네이티브 설정 (application-common.yml)
- **호스트 네임 표준화**: 로컬 중심의 설정을 탈피하고, K8s 서비스 명칭(`user`, `auth`, `redis`, `elasticsearch`, `redpanda`)을 기본값으로 사용하도록 [release](file:///c:/Users/pc/IdeaProjects/beadv4_4_semicolon_BE/product/src/main/java/dukku/product/boundedContext/product/entity/Product.java#216-227) 프로파일을 전면 개편했습니다.
- **통신 자동화**: 별도의 환경변수 주입 없이도 서비스 간 내부 DNS 주소로 자동 연결되도록 구성했습니다.

### DB 및 스토리지 보완
- **PostgreSQL 영속성 확보**: PVC(Persistent Volume Claim)를 도입하여 DB 재시작 시 데이터가 유실되지 않도록 인프라를 구축했습니다. (1Gi 할당)
- **pgvector 익스텐션 패치**: AI 서비스용 벡터 검색을 지원하기 위해 `pgvector` 라이브러리가 포함된 이미지를 커스텀 적용했습니다.
- **Elasticsearch nori 플러그인**: 한국어 검색 품질 유지를 위해 `analysis-nori` 플러그인을 Pod 내에 직접 설치(수동 조치 필요)하는 가이드를 마련했습니다.

---

## 4. 개별 서비스 주요 수리 내역

- **AI 서비스**:
  - [application-common.yml](file:///c:/Users/pc/IdeaProjects/beadv4_4_semicolon_BE/common/src/main/resources/application-common.yml) 임포트 누락으로 인한 Redis 연결 실패(localhost 접속) 문제 해결.
  - `show_sql` 오타 수정.
- **User 서비스**:
  - 누락되었던 `JavaMailSender` 관련 필수 속성(spring.mail.*) 복구.
- **Auth 서비스**:
  - (사전 조치) User 서비스와의 통신 방식을 직접 DB 참조가 아닌 REST API 참조로 고도화 준비.
- **InitData**:
  - 모듈 간 의존성 없이 독립적으로 구동될 수 있도록 고정 UUID 기반으로 InitData 로직 분리.

---

## 5. 담당자 복귀 시 확인 사항 (Critical)

1. **DB 스키마 정책**: 현재 초기 테이블 생성을 위해 Secret(`semicolon-env`)의 `SPRING_JPA_HIBERNATE_DDL_AUTO`를 [update](file:///c:/Users/pc/IdeaProjects/beadv4_4_semicolon_BE/product/src/main/java/dukku/product/boundedContext/product/entity/Product.java#169-187)로 설정해 둔 경우가 있습니다. 모든 서비스가 안착하면 다시 `validate`로 수동 원복을 권장합니다.
2. **ES 플러그인**: Elasticsearch Pod이 재구성될 경우 `analysis-nori` 플러그인을 다시 설치해 주어야 `product` 서비스가 정상 기동됩니다.
3. **DB 인스턴스**: 신규 Postgres 인프라에는 `ai_service` 등 9개 데이터베이스를 `psql` 명령어로 직접 생성해 주어야 합니다. (현재 생성 완료 상태)

---

**작성자**: AI Assistant (Antigravity)
**협업자**: @USER
**수행 기간**: 2026-02-22 ~ 2026-02-24
