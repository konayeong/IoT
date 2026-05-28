# IoT Rule Engine
# Step1~3
## 기본 엔진 구현 + 확장 아키텍처 설계
### NodeRegistry
> 노드 생성 중앙 관리소 역할

- 문자열 타입명과 실제 노드 생성 로직을 연결해주는 역할
- `typeName → NodeFactory → Node 생성`
- **NodeFactory**
    - config를 받아 실제 Node 생성
    - 노드 생성 함수형 인터페이스
        - 람다 등록이 가능함
    - Factory ?
        - 노드마다 생성 규칙이 다름
        - 공통 생성 인터페이스가 필요

# Step 4~5
### 플로우 정의 포맷 설계
- 엔진은 외부 JSON/YAML 정의를 읽어서 플로우를 구성(노드 생성, 포트 연결, 플로우 실행)해야 한다
- 플로우 정의 예시 (JSON)

    ```json
    {
      "id": "temperature-monitoring",
      "name": "온도 모니터링 플로우",
      "description": "MQTT 센서 데이터를 수신하여 임계값 초과 시 알림",
      "nodes": [
        {
          "id": "sensor",
          "type": "MqttSubscriber",
          "config": {
            "broker": "tcp://localhost:1883",
            "topic": "sensor/temp",
            "qos": 1
          }
        },
        {
          "id": "rule",
          "type": "ThresholdFilter",
          "config": {
            "field": "value",
            "operator": ">",
            "threshold": 30
          }
        },
        {
          "id": "alert",
          "type": "MqttPublisher",
          "config": {
            "broker": "tcp://localhost:1883",
            "topic": "alert/temp"
          }
        }
      ],
      "connections": [
        { "from": "sensor:out", "to": "rule:in" },
        { "from": "rule:out", "to": "alert:in" }
      ]
    }
    ```
### 플러그인 아키텍처 개념 설계
