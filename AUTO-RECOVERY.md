# 자동 복구 (Auto-Recovery) 모니터링 전략

`BaseSystemException`과 같은 시스템 장애 발생 시, 이를 감지하고 자동으로 복구(Action) 로직을 수행하는 서비스(Action Service)를 개발하기 위한 아키텍처 전략 가이드입니다.

## 🏆 추천 전략

### 1. 🥇 Storage & Query Layer (Loki Ruler / Grafana Alerting)
**"로그가 쌓이면, 규칙에 따라 감지하고, Action Service로 Webhook을 전송한다."**

가장 표준적이고 안정적인 확장성을 제공하는 방식입니다.

*   **작동 방식**:
    1.  **감지**: Grafana(또는 Loki Ruler)가 주기적으로 LogQL을 실행 (예: 1분마다).
        *   Query: `{is_fix_required="true"}`
    2.  **알림**: 조건이 충족되면 AlertManager가 **Action Service**의 API(예: `POST /api/v1/fix`)로 **Webhook** 전송.
    3.  **실행**: Action Service는 Payload(에러 상세, Trace ID 등)를 받아 자동 복구 로직 수행.
*   **장점**:
    *   **비동기 & 디커플링 (Decoupling)**: 로깅 시스템과 복구 시스템이 서로 영향을 주지 않고 완벽히 분리됩니다.
    *   **제어 용이성 (Throttling)**: "10초에 100건 발생해도 알림은 1번만 발송"과 같은 그룹화 및 속도 조절이 쉽습니다.
    *   **효율성**: 별도의 Polling 프로세스가 없으므로 불필요한 조회 부하가 없습니다.

---

### 2. 🥈 Collection Layer (Grafana Alloy Forking)
**"로그를 수집하는 순간, Loki에도 보내고 Action Service에도 바로 쏜다."**

가장 빠른 실시간성(Real-time)이 최우선일 때 사용하는 방식입니다.

*   **작동 방식**:
    *   Alloy 설정(`config.alloy`)에서 로그 스트림을 분기(Fork)합니다.
    *   Stream A -> Loki 저장소 (저장용)
    *   Stream B -> `loki.process` (필터링: `is_fix_required="true"`) -> Action Service HTTP Endpoint (실행용)
*   **장점**:
    *   **초저지연 (Low Latency)**: Loki에 저장되고 인덱싱되는 시간을 기다릴 필요 없이, 로그 발생 수 밀리초 내에 반응합니다.
*   **단점**:
    *   **복잡도 증가**: Action Service가 스파이크성 트래픽(Log Storm)을 직접 감당해야 하며, 중복 제거 로직을 직접 구현해야 합니다.

---

### 3. 🥉 External Monitoring Service (Polling) - **비추천**
**"별도 서비스가 무한루프를 돌며 Loki에게 `새로운 에러 있어?` 라고 계속 묻는다."**

*   **작동 방식**:
    *   Action Service가 스케줄러를 통해 10초마다 Loki API(`query_range`)를 직접 호출.
*   **단점**:
    *   **일관성 문제**: 조회 시점과 데이터 적재 시점의 미세한 차이로 인해 로그가 누락되거나 중복 처리될 위험이 큽니다.
    *   **리소스 낭비**: 장애가 없는 평시에도 계속 API를 호출하여 Loki와 네트워크에 불필요한 부하를 줍니다.

---

## 💡 결론
**Grafana Alerting + Webhook** 방식을 1순위로 고려하는 것을 권장합니다. 현재 Docker Compose 환경에 이미 구성된 Grafana의 Alerting 기능을 활용하면, 추가적인 인프라 구축 없이 바로 연동이 가능합니다.

---

## 🤖 실전 테스트 (End-to-End Test)

이 프로젝트에 구현된 자동 복구 파이프라인을 다음 단계로 테스트할 수 있습니다.

### 1. 테스트 환경 구조
- **Action Service**: 포트 `5001`(Host) / `5000`(Container)에서 대기 중인 Python 서버.
- **Grafana Alerting**: Loki에 쌓이는 로그 중 `is_fix_required="true"` 필드를 감시하여 Webhook 발송.

### 2. 검증 단계
1.  **시스템 에러 유발**:
    ```bash
    curl http://localhost:8080/demo/system-error
    ```
    이 명령은 `BaseSystemException`을 발생시키며, 로그에 `is_fix_required: "true"` 마커가 포함됩니다.

2.  **Loki 인덱싱 대기**: 수집된 로그가 Loki에 저장되고 Grafana Alert Rule(10초 주기)에 의해 탐지될 때까지 잠시 기다립니다.

3.  **복구 액션 확인**:
    Action Service의 로그를 확인하여 Grafana로부터 Webhook이 정상적으로 도착했는지 확인합니다.
    ```bash
    docker logs action-service
    ```
    성공 시 로그에 `>>> AUTOMATIC FIX INITIATED <<<` 및 `>>> FIX COMPLETED <<<` 메시지가 출력됩니다.

