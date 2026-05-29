# IoT Rule Engine
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
