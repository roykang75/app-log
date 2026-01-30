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
Designed for **Advanced Agentic Coding** environments.
