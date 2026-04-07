# 로그인 & 세션 관리 서버

Spring Boot 기반의 JWT 인증/인가 서버입니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Build | Gradle |
| DB | MySQL 8.0 |
| ORM | Spring Data JPA (Hibernate) |
| 인증 | JWT (jjwt 0.12.x) |
| 캐시 | Redis 7 |
| Rate Limiting | Bucket4j |
| 컨테이너 | Docker Compose |

---

## 실행 방법

### 방법 1: Docker Compose (권장)

```bash
# JAR 빌드
./gradlew bootJar

# 전체 스택 실행 (MySQL + Redis + App)
docker compose up --build
```

앱이 `http://localhost:8080` 에서 실행됩니다.

### 방법 2: 로컬 직접 실행

**사전 요구사항**: MySQL 8.0, Redis 7 실행 중

```bash
# MySQL 데이터베이스 생성
mysql -u root -p -e "CREATE DATABASE manage_db; CREATE USER 'manage_user'@'localhost' IDENTIFIED BY 'manage_pass'; GRANT ALL ON manage_db.* TO 'manage_user'@'localhost';"

# 애플리케이션 실행
./gradlew bootRun
```

### 테스트 실행

```bash
./gradlew test
```

---

## API 명세

### 인증 (Auth)

#### 회원가입
```
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```
- 성공: `201 Created` `{ "message": "회원가입이 완료되었습니다." }`
- 중복 이메일: `409 Conflict`
- 유효성 오류: `400 Bad Request`

#### 로그인
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```
- 성공: `200 OK`
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "550e8400-e29b-41d4-a716-...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```
- 실패: `401 Unauthorized`

#### 토큰 재발급
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-..."
}
```
- 성공: `200 OK` (새 Access Token + Refresh Token 쌍 반환)
- 만료/미존재: `401 Unauthorized`

#### 로그아웃
```
POST /api/auth/logout
Authorization: Bearer {accessToken}
```
- 성공: `200 OK` `{ "message": "로그아웃되었습니다." }`

### 유저 정보

#### 내 정보 조회
```
GET /api/user/me
Authorization: Bearer {accessToken}
```
- 성공: `200 OK`
```json
{
  "id": 1,
  "email": "user@example.com",
  "role": "USER",
  "createdAt": "2026-04-06T12:00:00"
}
```
- 미인증: `401 Unauthorized`

---

## 설계 의도 및 기술 선택 이유

### JWT: Access Token + Refresh Token 분리
- **Access Token**: 짧은 만료(15분), Stateless 검증
- **Refresh Token**: UUID (JWT 아님) → Redis에 저장하여 즉시 폐기 가능
- JWT로 Refresh Token을 만들면 만료 전 강제 폐기가 불가능하므로 UUID 선택

### Refresh Token Rotation
- `/api/auth/refresh` 호출 시 기존 토큰 삭제 + 새 토큰 발급
- 토큰 탈취 시 피해 범위를 최소화

### Redis 캐싱 (선택 과제)
- TTL 자동 만료로 만료된 토큰 자동 정리
- O(1) 조회 성능
- DB와 이중 저장으로 데이터 무결성 유지

### Argon2 비밀번호 해시
- BCrypt는 취약점이 나온 이력이 있고 agron2가 더 안전하여 argon2를 선택

### Rate Limiting (Bucket4j)
- IP 단위 분당 20회 제한
- Token Bucket 알고리즘: 버스트 허용 + 지속적 제한
- `X-Forwarded-For` 헤더 지원 (프록시 환경)

### Spring Data JPA
- 타입 안전한 쿼리, 자동 DDL 관리

---

## ERD

[docs/erd.md](docs/erd.md) 참고

---

## 단위 테스트 결과

```bash
./gradlew test --info
```

| 테스트 클래스 | 테스트 수 | 내용 |
|--------------|-----------|------|
| JwtServiceTest | 3 | 토큰 생성/파싱, 만료, 변조 |
| AuthServiceTest | 4 | 회원가입, 중복, 로그인, 비밀번호 오류 |
| RefreshTokenServiceTest | 2 | 만료 토큰, 유효 토큰 |
| AuthControllerTest | 3 | 회원가입 201/400, /me 401 |

테스트 결과 캡처: `build/reports/tests/test/index.html`
