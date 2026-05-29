# IoT Rule Engine
# Stage1
# Step1

## 환경 구축 & FBP 개념

> Maven 프로젝트 생성, 패키지 구조, FBP 5요소 이해

### FBP

- 독립적인 처리 단위(노드)들 사이로 데이터(메시지)가 흘러가는 방식
- 핵심 요소

| 요소 | 역할 | 비유 |
| --- | --- | --- |
| Node | 데이터를 처리하는 독립 단위 | 작업대 |
| Port | 노드의 입구(In)와 출구(Out) | 작업대의 입출력 선반 |
| Connection | 포트 사이를 연결하는 통로 | 컨베이어 벨트 |
| Message | 노드 사이를 이동하는 데이터 | 벨트 위의 부품 |
| Flow | 노드와 연결의 전체 구성도 | 공장 배치도 |

### 과제

![diagram](./docs/iot-fbp-diagram.png)

| 소속 패키지 | 클래스/인터페이스 | 역할 |
| --- | --- | --- |
| core | `Node` | 모든 노드가 구현할 인터페이스 |
|  | `InputPort` | 메시지를 받는 입구 인터페이스 |
|  | `OutputPort` | 메시지를 보내는 출구 인터페이스 |
|  | `Connection` | 포트 사이를 연결하는 통로 |
|  | `Flow` | 노드와 연결의 전체 구성도 |
|  | `FlowEngine` | 여러 Flow를 등록/시작/정지하는 관리자 |
| message | `Message` | 노드 사이를 이동하는 데이터 |
| node | `Node 구현체` | Node 인터페이스를 구현한 구체적인 노드들 |
| runner | `Main` | 엔진 실행 진입점 |

## Step2
## Node / Message 설계

> 인터페이스·제네릭, Message(불변), PrintNode

### Message
- 불변 객체
    - 한번 만들어지면 내부 상태가 절대 바뀌지 않는 객체
    - 설계 이유 : 나중에 여러 스레드가 동시에 같은 메시지를 읽어도 안전
    - 핵심 구현 방법
        1. 생성자에서 외부 Map 복사 : Collections.unmodifiableMap(new HashMap<>(payload));
        2. 데이터 추가 시 새 객체 반환
- 가변 객체
    - 문제 : 누가 언제 바꿨는지 추적 불가

### Node, Message 에서 ID를 갖는 이유

- Node : 연결 정의, 실행 관리, 디버깅 & 로그
- Message
    - 메시지 추적, 중복 처리 / 재처리 방지
    - UUID : 전역 유일성, 분산 시스템 대비

### Timestamp

- 데이터 생성 추적 가능
- 시간 기반 처리 가능

## Step3

## Port & Connection 설계

> InputPort, OutputPort, Connection(Queue), FilterNode

### Node는 처리만 하고 전달은 Port + Connection이 한다

### Port

- Node 간의 결합도 낮추기
- **InputPort** : `Connection → InputPort → Node.process()`
- **OutputPort** : `Node → OutputPort → Connection 들`

### 과제

1. Connection
    - buffer를 사용하는 이유
        - 생산자(Output)와 소비자(Input)를 분리하기 위해서
        - 둘의 속도가 다를 수 있기 때문에

[구조]

![iot-fbp-3-7.png](./docs/iot-fbp-3-7.png)

![iot-fbp-3-8.png](./docs/iot-fbp-3-8.png)

## Step4

## 스레드 기초 & 동시성

> Thread, BlockingQueue, Connection 개선, 멀티스레드 파이프라인
>
- 각 노드가 자기 속도로 처리하고, 서로 기다리지 않도록 함

### Producer / Consumer

- Producer : 메시지 생성
- Consumer : 메시지 처리

### BlockingQueue

- 데이터가 없으면 자동으로 기다리는 큐
- **busy waiting**
    - poll() : 데이터가 없으면 null, 계속 확인해야 함
    - 해결 : take() - 데이터 없으면 자동 대기, 들어오면 바로 깨어남

### 과제

1. ArrayList vs synchronized vs BlockingQueue

| 방식 | 코드 길이 | 예외 처리 | CPU 사용률 |
| --- | --- | --- | --- |
| ArrayList (busy waiting) | 짧음 | 없음 | ❌ 높음 (계속 검사) |
| synchronized + wait/notify | 김 | 복잡 | ⭕ 낮음 |
| BlockingQueue | 매우 짧음 | 단순 | ⭕ 낮음 |

## Step5

## AbstractNode & 설계 패턴

> 추상 클래스, 생명주기, Template Method, TimerNode
- 노드 구현의 중복을 제거하고, 구조를 표준화

### 생명주기
- initialize → (process 반복) → shutdown

### Template Method

- 전체 흐름은 고정, 핵심만 바꾼다
- process()가 공통 흐름을 정의하고, 핵심 로직은 하위 클래스에 위임


## Step6

## 노드 라이브러리 확장

> TransformNode, SplitNode, CounterNode, DelayNode

### TransformNode

- 데이터 변환을 수행하는 범용 노드
- `Function<Message, Message>`
    - 변환 로직을 외부에서 주입
    - 변환마다 클래스를 새로 만들지 않기 위해서

![iot-fbp-6-4.png](./docs/iot-fbp-6-4.png)

## Step7
## Flow 클래스 & 그래프 구조
> Flow, 메서드 체이닝, validate, 순환 참조 탐지(DFS)

### Flow
- 노드 + 연결을 하나의 그래프로 관리하는 객체
- 구조 관리(노드, 연결)
- 생명주기 관리

## Step8
## FlowEngine 구현
> ExecutorService, 상태 관리, 다중 플로우, CLI

### FlowEngine
- Flow를 실행하고 관리하는 최상위 객체
- 여러 Flow의 실행을 통합/관리
- 역할
    - Flow 등록
    - Flow 시작 ( validate → initialize → 상태 변경)
    - Flow 정지
    - 전체 종료 : shutdown

### ExecutorService
- Thread Pool 사용

- Thread
    - 능동적 노드(데이터를 생성하는)
        - initialize()에서 생성
        - shutdown()에서 정리
        - ex. TimerNode
    - 수동적 노드
        - 스레드 없음
        - 메시지를 받으면 처리만 함
        - Connection 스레드가 receive() 호출 → process() 실행
    - Connection의 스레드
        - FlowEngine이 각 Connection마다 스레드 생성
        - 수동적 전달 (메시지를 옮기는 역할)만 담당
      
## Step9
## IoT 시나리오 적용
> SensorNode, ThresholdFilter, AlertNode, FileWriter
### 온도 / 습도 모니터링 플로우
![iot-fbp-9](./docs/iot-fbp-9.png)

## Step10
## 통합 테스트 & 리팩토링
> JUnit 5, CollectorNode, 약 125개 테스트, 리팩토링
### Collector

- 테스트에서 노드 출력을 검증하기 위한 노드
- PrintNode 대신 파이프라인 끝에 연결하여 콘솔 출력 없이 메시지 수신 결과를 검증 가능

### 최종 종합
![iot-fbp-10](./docs/iot-fbp-10.png)

# Stage2
# Step1
## 네트워크 + ProtocolNode
### ProtocolNode 핵심 책임
> 외부 통신 노드들의 공통 기능(연결, 상태, 재연결) 추상화
1. 연결 상태 관리
2. initialize() : 노드 시작 = 외부 연결 시작
3. shutdown() : 외부 자원 정리
4. reconnect() : 연결 끊기면 자동 재시도


### ProtocolNode Scheduler 구조
```java
initialize()
   │
   ├── connect 성공 → CONNECTED (끝)
   │
   └── connect 실패 → ERROR
             │
             ▼
      startReconnect()
             │
     (5초 후 실행 예약)
             │
             ▼
        reconnect()
             │
     ┌───────┴────────┐
     │                │
 connect 성공     connect 실패
     │                │
 CONNECTED      ERROR → startReconnect()
                        │
                        ▼
                 다시 5초 후 reconnect
```

# Step2
## MQTT
> 발행(Publish) / 구독(Subscribe) 기반 메시지 브로커
### 핵심 개념
```text
                          ┌─────────────┐
  [Sensor A] ──publish──→ │             │ ──→ [Subscriber 1]
                          │ MQTT Broker │
  [Sensor B] ──publish──→ │ (Mosquitto) │ ──→ [Subscriber 2]
                          │             │
                          └─────────────┘
```
- Broker : 메시지 중개자. 발행자와 구독자를 연결
- Topic : 메시지의 주소. 계층 구조
- QoS : 메시지 전달 보장 수준 (0, 1, 2)
- Retained Message : Broker가 토픽의 마지막 메시지 저장. 새 구독자가 연결하면 즉시 최신 값을 받을 수 있음
- Last Will and Testament (LWT) : 클라이언트가 비정상 종료 시 Broker는 미리 등록된 유언 메시지 발행

### MqttSubscriberNode / MqttPublisherNode
- **SubscriberNode** : MQTT Broker에서 들어오는 메시지를 FBP 메시지로 변환해서 Flow에 주입하는 Source Node
```text
MQTT Broker                   FBP 플로우
                                   ┌──────────────────────┐
 topic: sensor/temp                │  MqttSubscriberNode  │
  ──(MQTT message)──→ callback ──→ │  "out" OutputPort    │──→ [next node]
                                   └──────────────────────┘
```
- **PublisherNode** : FBP에서 나온 Message를 MQTT Broker로 “외부로 내보내는 Sink Node”
```text
FBP Flow                                 MQTT Broker
                  ┌────────────────────┐
[Previous Node]──→│  MqttPublisherNode │
                  │  "in" InputPort    │──(MQTT Publish)──→ topic: alert/temp
                  └────────────────────┘
```

# Step3
## MODBUS
> 마스터-슬레이브 구조 (슬레이브는 먼저 데이터를 보내지 않는다)
### MODBUS TCP 프레임 구조
- MBAP Header (7byte)
    - MODBUS TCP 고유의 헤더
    - 모든 요청과 응답에 포함

        ``` 
        바이트 위치:  [0][1]     [2][3]    [4][5]          [6]
                   ──────     ──────    ──────          ───
        의미:       트랜잭션 ID  프로토콜 ID  길이(이후 바이트수) 유닛 ID
                   (2byte)    (2byte)   (2byte)         (1byte)
        ```

      | **필드** | **크기** | **설명**                             |
                    | --- | --- |------------------------------------|
      | Transaction ID | 2 바이트 | 요청/응답 쌍을 식별. 요청에서 보낸 값이 응답에 그대로 돌아옴 |
      | Protocol ID | 2 바이트 | 항상`0x0000`(MODBUS 프로토콜)            |
      | Length | 2 바이트 | 이 필드 이후의 바이트 수 (Unit ID + PDU 길이)  |
      | Unit ID | 1 바이트 | 슬레이브 ID. TCP에서는 보통 `0x01`또는`0xFF`  |

- PDU (5 byte)

  ![modbus-tcp](./docs/modbus-tcp-frame.png)

### Modbus TCP 응답
1. 정상 응답 : FC = 0x03
2. 에러 응답 : FC = 0x83 = 원래 FC + 0x80 = MSB(최상위 비트)가 1로 바뀜
- 검증 방법
  - (fc & 0x80)이 1이면 Exception Response

# Step4
## Protocol Node 통합 / Rule 처리
### RuleNode
- 조건 true -> match
- 조건 false -> mismatch
- **규칙 표현 방식**
  - Java Predicate (코드 내 정의)
  ```java
  RuleNode rule = new RuleNode("temp-rule", msg -> {
      Double temp = msg.get("temperature");
      return temp != null && temp > 30.0;
  });
    ```
  - 문자열 기반 조건식
  ```java
  RuleNode rule = new RuleNode("temp-rule", "temperature > 30.0");
  ```
  - 복합 규칙 (AND/OR)
  ```java
  CompositeRule rule = new CompositeRule("complex", CompositeRule.Operator.AND);
  rule.addCondition("temperature", ">", 30.0);
  rule.addCondition("humidity", ">", 70.0);
  ```

# Stage3
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
  
# Stage3+
## production FBP 정의
> Node는 실행 흐름을 제어하지 않고, 모든 실행은 Engine + Queue + Worker Pool이 담당한다.

### 전체 아키텍처 
```text
        ┌──────────────┐
        │   Producer    │ (Node.process / external input)
        └──────┬───────┘
               ↓
        ┌──────────────┐
        │ OutputPort   │
        └──────┬───────┘
               ↓
        ┌──────────────┐
        │ Connection   │ (QUEUE + backpressure)
        └──────┬───────┘
               ↓
   ┌─────────────────────────┐
   │   FlowEngine Workers    │ (thread pool)
   └──────────┬──────────────┘
              ↓
        ┌──────────────┐
        │ InputPort    │
        └──────┬───────┘
               ↓
        ┌──────────────┐
        │ Consumer Node │
        └──────────────┘
```

### 핵심 원칙 5개
1. Node는 "pure function + ports"
    - input -> process -> output
2. Connection = 단일 책임 (queue only)
    - BlockingQueue<Message>
3. Engine = 유일한 실행 주체
    - thread pool 관리
    - polling / dispatch 담당
    - metrics 담당
4. Push / Pull 혼합 금지
5. sync execution 제거
    - process() -> 직접 결과 기대 x
    - 항상 engine 기반 flow
