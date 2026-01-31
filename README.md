# 실시간 예외 감지 및 자동 수정 시스템 (app-log)

Spring Boot 환경에서 발생하는 로그를 분석하여 실시간으로 코드를 수정/재배포하는 시스템의 핵심 로깅 모듈입니다. 비즈니스 에러와 시스템 장애를 명확히 구분하고 자동화 도구가 추적 가능한 구조화된 데이터를 제공합니다.

## 🚀 핵심 설계 원칙

1.  **구별 가능성 (Differentiability)**: 로그 레벨과 마커를 통해 코드 수정이 필요한 시스템 장애인지 단순 사용자 오류인지 즉시 판별 가능.
2.  **구조화된 로깅 (Structured Logging)**: JSON 형식을 사용하여 자동화 시스템(Fixer)이 파싱하기 용이하게 구성.
3.  **추적성 (Traceability)**: 모든 요청에 `trace_id`를 부여하고 `user_id`를 추적하여 분산 환경에서도 흐름 파악 가능.

## 🛠 기술 스택

-   **Framework**: Spring Boot 3.4.2
-   **Java**: 21
-   **Build Tool**: Gradle
-   **Logging**: SLF4J + Logback + Logstash Logback Encoder
-   **API Doc**: SpringDoc OpenAPI (Swagger UI)

## 🏗 주요 기능

### 1. 예외 계층 구조 (Exception Hierarchy)
-   `BaseBusinessException`: 비즈니스 로직상의 예외 (HTTP 4xx 대응, WARN 레벨). 사용자의 입력 오류나 잔액 부족 등을 처리.
-   `BaseSystemException`: 로직 결함이나 인프라 장애 (HTTP 5xx 대응, ERROR 레벨). 코드 수정이나 시스템 점검이 필요한 경우.

### 2. 구조화된 로그 필드 (JSON)
모든 로그는 다음과 같은 필드를 포함하는 JSON 형태로 기록됩니다.
-   `trace_id`: 요청 고유 식별자
-   `user_id`: 요청을 보낸 사용자 ID (X-User-Id 헤더에서 추출)
-   `exception_category`: `BUSINESS` 또는 `SYSTEM`
-   `is_fix_required`: 시스템 에러 시 `true`로 설정되어 자동화 시스템의 트리거 역할 수행
-   `stack_trace`: 에러 발생 시 전체 스택 트레이스 포함

### 3. MDC (Mapped Diagnostic Context) 필터
`MdcLoggingFilter`를 통해 모든 비즈니스 로직 이전에 추적 식별자를 컨텍스트에 주입합니다.

## 🖥 실행 방법

```bash
./gradlew bootRun
```

-   **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
-   **API 테스트 엔드포인트**:
    -   비즈니스 에러 발생 (409): `GET /demo/business-error`
    -   시스템 장애 발생 (500): `GET /demo/system-error`

## 📊 로그 샘플

### [SYSTEM ERROR] - 코드 수정 필요 케이스
```json
{
  "timestamp" : "2026-01-30T19:40:56.721607+09:00",
  "level" : "ERROR",
  "message" : "[SYSTEM_ERROR] Message: Simulated System NullPointerException...",
  "trace_id" : "0f0685e6-8947-46ad-b774-1a343c960894",
  "exception_category" : "SYSTEM",
  "user_id" : "user_123",
  "is_fix_required" : "true",
  "stack_trace" : "java.lang.NullPointerException: ..."
}
```


---

## 🐳 Docker & Logging Pipeline

이 프로젝트는 **Spring Boot -> Grafana Alloy -> Loki -> Grafana**로 이어지는 완전한 로깅 파이프라인을 Docker Compose로 제공합니다.

### 아키텍처
1.  **Spring Boot App**: `Loki4jAppender`를 사용하여 로그를 Alloy로 직접 전송.
2.  **Grafana Alloy**: 로그 수집기. Spring App으로부터 로그를 받아 Loki로 전달.
3.  **Loki**: 로그 집계 및 저장소.
4.  **Grafana**: 로그 시각화 및 대시보드.

### 실행 방법
Docker Desktop이 실행 중인지 확인 후 다음 명령어를 실행합니다.

```bash
docker-compose up -d --build
```

### 서비스 접속
-   **Spring App**: [http://localhost:8080](http://localhost:8080)
-   **Grafana**: [http://localhost:3000](http://localhost:3000) (계정: `admin` / `admin`)

## 🔍 Grafana LogQL 가이드 (Log Query)

Grafana의 **Explore** 메뉴에서 **Loki** 데이터 소스를 선택하고 다음 쿼리를 활용하세요.

### 1. 전체 로그 조회
```logql
{app="app-log"}
```

### 2. 에러 로그만 필터링 (추천)
자동 수정이 필요한 시스템 에러만 조회합니다.
```logql
{app="app-log"} | json | is_fix_required="true"
```

### 3. 특정 Trace ID 추적
특정 요청의 흐름을 추적할 때 유용합니다.
```logql
{app="app-log"} | json | trace_id="YOUR_TRACE_ID"
```

### 4. 키워드 검색
특정 예외 클래스나 메시지를 포함하는 로그를 검색합니다.
```logql
{app="app-log"} |= "NullPointerException"
```

### 🚨 문제 해결: Grafana에서 로그가 안 보일 때

만약 Grafana에서 쿼리를 실행했는데 `No logs found` 메시지가 나온다면 다음 단계를 확인하세요.

1.  **Time Range 확인**: 우측 상단 시간 설정이 너무 짧게 되어있지 않은지 확인합니다. Docker 컨테이너 시간차 등을 고려하여 **"Last 1 hour"**이상으로 설정하는 것을 권장합니다.
2.  **데이터 소스 확인**: Alloy가 Loki로 데이터를 정상적으로 보내는지 확인하려면 로컬에서 다음 명령어로 직접 Loki API를 찔러볼 수 있습니다.
    ```bash
    # 최근 로그 10개 조회
    curl -G "http://localhost:3100/loki/api/v1/query_range" \
      --data-urlencode 'query={app="app-log"}' \
      --data-urlencode 'limit=10'
    ```
    데이터가 리턴된다면 Loki 저장소에는 정상적으로 쌓이고 있는 것입니다.

