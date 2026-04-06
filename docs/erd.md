# ERD 다이어그램

## 테이블 관계

```mermaid
erDiagram
    users {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR(255) email UK "NOT NULL"
        VARCHAR(255) password "NOT NULL (BCrypt hash)"
        VARCHAR(20) role "NOT NULL DEFAULT 'USER'"
        DATETIME created_at "NOT NULL"
        DATETIME updated_at "NOT NULL"
    }

    refresh_tokens {
        BIGINT id PK "AUTO_INCREMENT"
        BIGINT user_id FK UK "NOT NULL"
        VARCHAR(255) token UK "NOT NULL (UUID)"
        DATETIME expires_at "NOT NULL"
        DATETIME created_at "NOT NULL"
    }

    users ||--o| refresh_tokens : "1:0..1"
```

## 설계 설명

### users
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT | PK, Auto Increment |
| email | VARCHAR(255) | 유니크, 로그인 ID |
| password | VARCHAR(255) | BCrypt 해시값 |
| role | VARCHAR(20) | USER / ADMIN |
| created_at | DATETIME | 생성 시각 |
| updated_at | DATETIME | 수정 시각 |

### refresh_tokens
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT | PK, Auto Increment |
| user_id | BIGINT | FK → users.id (UNIQUE: 1유저 1토큰) |
| token | VARCHAR(255) | UUID 문자열, 유니크 |
| expires_at | DATETIME | 만료 시각 (7일) |
| created_at | DATETIME | 생성 시각 |

## Redis 키 구조

| 키 패턴 | 값 | TTL |
|---------|-----|-----|
| `refresh_token:{userId}` | UUID 문자열 | 7일 |
