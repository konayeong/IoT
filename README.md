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
## 플로우 정의 포맷 설계
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
  
### Definition 계층
> JSON/YAML 설정 표현
- JSON, YAML을 메모리 객체로 옮긴 게 ~Definition (설정 데이터), 실행 안 함 

### 핵심 동작 흐름
```text
JSON 파일
   │
   ▼
FlowParser.parse(InputStream) → FlowDefinition
   │
   ▼
FlowDefinition + NodeRegistry → Flow 객체 생성
   │  (NodeRegistry로 각 노드 인스턴스 생성, Connection으로 연결)
   ▼
FlowManager.deploy(Flow) → FlowEngine에 등록 및 실행
```
# Step 6~7
## 플러그인 아키텍처 개념 설계
### ServiceLoader
> Java의 SPI(Service Provider Interface) 기반 확장 메커니즘
- 인터페이스 구현체를 자동으로 탐색하고 로드할 수 있다
- 본 프로젝트에서는: `ServiceLoader<NodeProvider>`를 사용하여 플러그인 노드 제공자를 자동 발견한다.
- 플러그인 JAR 내부에는 다음 파일이 필요하다: `META-INF/services/com.fbp.engine.plugin.NodeProvider`
- 파일 내용에는 구현 클래스의 전체 이름(FQCN)을 작성한다
  - 예시: `com.example.plugin.CustomNodeProvider`
- 이를 통해 엔진 수정 없이 새로운 노드를 동적으로 확장할 수 있다.

### PluginManager
> 플러그인 시스템의 진입점 역할을 담당
- 주요 역할

  - ClassPath 기반 플러그인 로드
  - plugins/ 디렉토리의 외부 JAR 플러그인 로드
  - NodeProvider 탐색
  - 플러그인 노드를 NodeRegistry에 자동 등록
  - 타입 충돌 및 잘못된 플러그인 검증 처리

- 동작 흐름
```text
PluginManager
    ↓
ServiceLoader<NodeProvider>
    ↓
NodeDescriptor
    ↓
NodeRegistry.register(...)
```
- 플러그인이 등록되면 Flow JSON에서 일반 노드처럼 사용할 수 있다

### PluginClassLoader
- 외부 JAR 파일을 런타임에 읽어서 클래스 로딩하는 클래스 로더
- 일반적인 자바 실행
    - classpath에 있는 클래스만 로딩 가능
    - 외부 JAR는 자동으로 못 읽음
- pluginClassLoader 있으면
    - 외부 JAR → 클래스 로딩 가능

# Step8
## 모니터링과 관리 API
### 목표
- 외부에서 현재 상태를 확인하고 HTTP 요청으로 엔진을 제어할 수 있어야 한다
### 1. 메트릭 수집
- MetricsCollector, NodeMetrics
- 각 노드가 메시지를 몇 개 처리했는지, 에러가 몇 번 발생했는지, 평균 처리 시간이 얼마인지 같은 실행 정보 기록
- **NodeMetrics** : 실제 실시간 메트릭 저장소
- **MetricsCollector** : 전체 메트릭 관리자
- **FlowMetrics** : NodeMetrics들을 묶어서 보여주는 집계 결과

### 2. HTTP 관리 API
- HttpServer를 사용해서 REST API 서버를 만든다
- 노드 실행
    - → MetricsCollector가 메트릭 기록
    - → 사용자가 HTTP 요청 전송
    - → Handler가 메트릭 조회
    - → JSON 응답 반환


### REST API 엔드포인트

|Method|Path|설명|요청본문|응답|
|---|---|---|---|---|
|GET|/flows|실행 중인 플로우 목록|	—	|[{id, name, status}]|
|POST|/flows|새 플로우 배포플로우 정의| JSON	|{id, status}|
|DELETE|	/flows/{id}|	플로우 중지 및 삭제|	—	|{message}|
|GET|	/flows/{id}/metrics|	플로우 메트릭 조회	|—	|{nodes: [{id, processed, errors, avgTime}]}|
|GET|	/nodes/{id}/stats|	노드 상세 통계	|—	|{processed, errors, avgTime, queueSize}|
|GET|	/health|	엔진 상태	|—	|{status, uptime, flowCount}|