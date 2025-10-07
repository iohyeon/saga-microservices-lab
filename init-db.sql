-- 각 서비스별 데이터베이스 생성
CREATE DATABASE account_db;
CREATE DATABASE ledger_db;
CREATE DATABASE risk_db;
CREATE DATABASE notification_db;
CREATE DATABASE orchestrator_db;

-- 사용자 권한 부여
GRANT ALL PRIVILEGES ON DATABASE account_db TO saga;
GRANT ALL PRIVILEGES ON DATABASE ledger_db TO saga;
GRANT ALL PRIVILEGES ON DATABASE risk_db TO saga;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO saga;
GRANT ALL PRIVILEGES ON DATABASE orchestrator_db TO saga;