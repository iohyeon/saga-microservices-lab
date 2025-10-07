# Account Service 테스트 가이드

## 1. 인프라 실행

```bash
docker compose up -d
```

확인:
```bash
docker compose ps
# PostgreSQL (5432), Redis (6379), Kafka (9092) 실행 중이어야 함
```

## 2. 테스트 데이터 생성

```bash
docker exec -i saga-postgres psql -U saga -d account_db << EOF
INSERT INTO accounts (account_id, balance, held_amount, version, created_at, updated_at)
VALUES
    ('ACC-A', 10000, 0, 0, NOW(), NOW()),
    ('ACC-B', 5000, 0, 0, NOW(), NOW())
ON CONFLICT (account_id) DO NOTHING;
EOF
```

## 3. Account Service 실행

```bash
./gradlew :account-service:bootRun
```

## 4. API 테스트

### 4.1 잔액 조회
```bash
curl http://localhost:8081/accounts/ACC-A
# 응답: {"accountId":"ACC-A","balance":10000,"heldAmount":0,...}
```

### 4.2 잔액 Hold (송금 예약)
```bash
curl -X POST http://localhost:8081/accounts/hold \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC-A",
    "holdId": "HOLD-001",
    "amount": 3000
  }'
# 응답: {"holdId":"HOLD-001","status":"HELD",...}
```

### 4.3 잔액 확인 (held 반영됨)
```bash
curl http://localhost:8081/accounts/ACC-A
# 응답: {"balance":10000,"heldAmount":3000} → 사용가능: 7000
```

### 4.4 Hold Commit (확정)
```bash
curl -X POST http://localhost:8081/accounts/commit \
  -H "Content-Type: application/json" \
  -d '{"holdId": "HOLD-001"}'
# 응답: {"status":"committed"}
```

### 4.5 잔액 확인 (실제 차감됨)
```bash
curl http://localhost:8081/accounts/ACC-A
# 응답: {"balance":7000,"heldAmount":0}
```

### 4.6 입금
```bash
curl -X POST http://localhost:8081/accounts/ACC-B/credit \
  -H "Content-Type: application/json" \
  -d '{"amount": 3000}'

curl http://localhost:8081/accounts/ACC-B
# 응답: {"balance":8000,...}
```

## 5. 보상 트랜잭션 테스트 (Release)

```bash
# Hold 생성
curl -X POST http://localhost:8081/accounts/hold \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC-A",
    "holdId": "HOLD-002",
    "amount": 2000
  }'

# Release (취소)
curl -X POST http://localhost:8081/accounts/release \
  -H "Content-Type: application/json" \
  -d '{"holdId": "HOLD-002"}'

# 잔액 확인: balance는 그대로, heldAmount만 감소
curl http://localhost:8081/accounts/ACC-A
```

## 예상 결과

| 작업 | ACC-A balance | ACC-A heldAmount | ACC-B balance |
|------|---------------|------------------|---------------|
| 초기 | 10000 | 0 | 5000 |
| Hold 3000 | 10000 | 3000 | 5000 |
| Commit | 7000 | 0 | 5000 |
| B 입금 3000 | 7000 | 0 | 8000 |
