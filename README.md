# IoT Rule Engine
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