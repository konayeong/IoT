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
### 