# Quick Start Guide

## ✅ 2단계 완료: account-service 구현

### 구현된 기능

1. **Account 엔티티**: 잔액(balance) + 예약금액(heldAmount) 관리
2. **AccountHold 엔티티**: Hold/Commit/Release 상태 추적
3. **4개 API**:
   - `GET /accounts/{accountId}` - 잔액 조회
   - `POST /accounts/hold` - 잔액 예약
   - `POST /accounts/commit` - 예약 확정 (실제 차감)
   - `POST /accounts/release` - 예약 취소 (보상 트랜잭션)
   - `POST /accounts/{accountId}/credit` - 입금

---

## 🚀 실행 방법

### 1. Docker Desktop 실행
macOS에서 Docker Desktop 앱을 실행하세요.

### 2. 인프라 시작
```bash
/Applications/Docker.app/Contents/Resources/bin/docker compose up -d
# 또는 PATH에 docker가 있다면: docker compose up -d
```

확인:
```bash
docker compose ps
# postgres, redis, kafka, zookeeper 실행 중
```

### 3. 테스트 데이터 생성
```bash
docker exec -i saga-postgres psql -U saga -d account_db << EOF
INSERT INTO accounts (account_id, balance, held_amount, version, created_at, updated_at)
VALUES
    ('ACC-A', 10000, 0, 0, NOW(), NOW()),
    ('ACC-B', 5000, 0, 0, NOW(), NOW())
ON CONFLICT (account_id) DO NOTHING;
EOF
```

### 4. account-service 실행
```bash
./gradlew :account-service:bootRun
```

### 5. API 테스트
자세한 테스트 시나리오는 `TEST-GUIDE.md` 참고

간단 테스트:
```bash
# 잔액 조회
curl http://localhost:8081/accounts/ACC-A

# Hold 생성
curl -X POST http://localhost:8081/accounts/hold \
  -H "Content-Type: application/json" \
  -d '{"accountId":"ACC-A","holdId":"HOLD-001","amount":3000}'

# Commit
curl -X POST http://localhost:8081/accounts/commit \
  -H "Content-Type: application/json" \
  -d '{"holdId":"HOLD-001"}'
```

---

## 📊 다음 단계

2단계 완료 후:
- ✅ account-service End-to-End 동작 검증
- 🔜 3단계: orchestrator + Saga State Machine 구현
- 🔜 4단계: ledger-service, risk-service 추가
