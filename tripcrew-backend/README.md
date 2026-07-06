# TripCrew Backend

TripCrew(여행 계획 공유 플랫폼)의 백엔드 REST API 서버.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Persistence | MyBatis 3.0 · MySQL 8 |
| Migration | Flyway |
| Cache / Pub-Sub | Redis 7 (캐싱 · 실시간 랭킹 ZSet) |
| Security | Spring Security · JWT (jjwt) · OAuth2 Client (Kakao · Naver) |
| Real-time | WebSocket (STOMP) |
| Resilience | Resilience4j (Circuit Breaker · Retry) |
| Build | Maven (wrapper 포함) |

> 설계 문서(ERD·스키마·결정 근거)는 저장소 루트 [`docs/`](../docs) 참고:
> [erd.md](../docs/erd.md) · [db/schema.sql](../docs/db/schema.sql) · [db-design-notes.md](../docs/db-design-notes.md)

## 프로젝트 구조

```
tripcrew-backend
├── pom.xml
├── mvnw / mvnw.cmd            # Maven Wrapper (로컬에 Maven 설치 불필요)
└── src/main
    ├── java/com/tripcrew
    │   ├── TripcrewBackendApplication.java   # 진입점 (@MapperScan)
    │   ├── common/                           # 공통: 설정·예외·응답 등 횡단 관심사
    │   │   ├── config/SecurityConfig.java     #   REST/stateless 보안 설정
    │   │   └── web/HealthController.java       #   GET /api/health
    │   ├── auth/        # F01 인증 (JWT, 회원가입/로그인, OAuth2 소셜 로그인)
    │   ├── user/        # users
    │   ├── attraction/  # F02 관광지(외부 API 캐시)
    │   ├── region/      # 시도/구군 등 지역 데이터
    │   ├── tripplan/    # F03/F04 여행계획·동선
    │   ├── coedit/      # F06 실시간 공동 편집(WebSocket)
    │   ├── review/      # F08 후기/평점 · like/ upload/ 이미지·찜
    │   ├── ranking/     # F07 실시간 랭킹(Redis ZSet)
    │   ├── notice/      # F10 공지
    │   ├── chat/        # F05 챗봇
    │   ├── admin/       # F09 관리자(대시보드·회원·신고)
    │   ├── report/  restriction/  # 신고·단계별 제재
    │   ├── inquiry/     # 1:1 문의
    │   ├── notification/ activity/  # 알림·활동 피드
    │   └── ...
    └── resources
        ├── application.properties
        ├── db/migration/        # Flyway 마이그레이션 (V1__init.sql ...)
        └── mappers/             # MyBatis mapper XML (feature 별 하위 폴더)
```

**패키지 컨벤션 — feature 기반(package-by-feature):**
각 기능 패키지는 그 안에 `controller / service / model(dto·mapper) / mapper.xml` 을 자체적으로 둔다.
계층(layer)별이 아니라 기능별로 묶어, 한 기능을 한 폴더에서 본다.

```
auth/
├── controller/AuthController.java
├── service/AuthService.java
├── model/dto/...
└── ...
```

MyBatis 매퍼 인터페이스(`@Mapper`)는 `*.mapper` 패키지에, 그에 대응하는 XML은 `resources/mappers/<feature>/`에 둔다.

## Maven 직접 실행

### 사전 준비 (각자 1회)
- **JDK 17** 이상
- **MySQL 8** 실행 중

> DB 접속 정보·JWT 시크릿은 **git에 올리지 않고 환경변수로 주입**한다. (`application.properties`가 `${ENV:기본값}` 형태로 참조)

### 환경변수 설정

`application.properties`에는 실제 비밀번호나 JWT 시크릿을 하드코딩하지 않는다.

```properties
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
jwt.secret=${JWT_SECRET:}
```

JWT 시크릿은 로컬에서 각자 생성한다.

```bash
openssl rand -base64 32
```

백엔드 실행 전, 같은 터미널 세션에서 환경변수를 설정한다.

```bash
export DB_USERNAME=root
export DB_PASSWORD="<본인 MySQL 비밀번호>"
export JWT_SECRET="<openssl rand -base64 32 결과값>"
```

위 `export` 방식은 현재 터미널 창에서만 유효하다. 터미널을 새로 열면 다시 설정해야 한다.
매번 입력하기 번거로운 경우에만 개인 PC의 `~/.zshrc` 같은 쉘 설정에 저장한다.
`~/.zshrc`는 이 저장소가 아니라 로컬 사용자 계정 설정이므로 git에 올라가지 않는다.

### 실행

```bash
# MySQL 8 에 DB 생성
mysql -u root -p -e "CREATE DATABASE tripcrew CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 첫 기동 시 Flyway 가 db/migration 의 V1__init.sql 을 자동 적용한다.
cd tripcrew-backend
./mvnw spring-boot:run
```

## Docker Compose 로컬 실행

프론트엔드, 백엔드, MySQL을 한 번에 띄우고 싶을 때는 저장소 루트에서 Docker Compose를 사용한다.
Docker Compose 방식에서는 로컬에 설치된 MySQL을 직접 실행하지 않아도 된다.

먼저 저장소 루트의 예시 파일을 복사해 개인 로컬용 `.env` 파일을 만든다.

```bash
cp .env.example .env
```

`.env`에서 각자 로컬 값을 채운다. `.env`는 git에 올라가지 않는다.

```bash
MYSQL_ROOT_PASSWORD=<본인 MySQL root 비밀번호>
MYSQL_DATABASE=tripcrew
MYSQL_PORT=3306

DB_URL=jdbc:mysql://mysql:3306/tripcrew?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=root
DB_PASSWORD=<본인 MySQL root 비밀번호>
JWT_SECRET=<openssl rand -base64 32 결과값>
BACKEND_PORT=8080
VITE_API_BASE_URL=http://localhost:8080/api
```

JWT 시크릿 생성:

```bash
openssl rand -base64 32
```

그 다음 실행한다.

```bash
docker compose up --build
```

실행 후 접속:

- 프론트엔드: http://localhost:5173
- 백엔드 API: `.env`의 `BACKEND_PORT` 값 사용. 기본 예시는 http://localhost:8080/api
- 헬스체크: 기본 예시는 http://localhost:8080/api/health
- MySQL: `.env`의 `MYSQL_PORT`, `MYSQL_DATABASE`, `DB_USERNAME`, `DB_PASSWORD` 값 사용

Docker 중심으로 개발할 때는 Docker가 표준 포트(`3306`, `8080`, `5173`)를 사용하도록 두고, 같은 포트를 쓰는 로컬 MySQL이나 직접 실행한 Spring Boot 서버는 먼저 종료한다.
백엔드 컨테이너는 Docker 내부 네트워크의 `mysql:3306`으로 접속하므로 `MYSQL_PORT`를 바꿔도 `DB_URL`은 그대로 두면 된다.
부득이하게 로컬 서비스와 Docker를 동시에 띄워야 한다면 `.env`에서 `MYSQL_PORT` 또는 `BACKEND_PORT`를 다른 값으로 바꿀 수 있다.

백그라운드 실행:

```bash
docker compose up -d --build
```

로그 확인:

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
```

중지:

```bash
docker compose down
```

DB 데이터까지 초기화:

```bash
docker compose down -v
```

관광 데이터 import:

관광 데이터 dump 파일은 용량과 관리 편의상 git에 포함하지 않고 별도로 공유받는다.
Docker DB를 실행한 뒤, 저장소 루트에서 아래 명령으로 import한다.

```bash
docker compose up -d
docker compose exec -T mysql sh -c 'mysql --default-character-set=utf8mb4 -u root -p"$MYSQL_ROOT_PASSWORD" tripcrew' < /path/to/tripcrew_dump.sql
```

정상 import 여부는 MySQL에 접속해 건수를 확인한다.

```bash
docker compose exec mysql mysql --default-character-set=utf8mb4 -u root -p tripcrew
```

```sql
SELECT COUNT(*) FROM sidos;
SELECT COUNT(*) FROM guguns;
SELECT COUNT(*) FROM contenttypes;
SELECT COUNT(*) FROM attractions;
```

`docker compose down`은 DB 데이터를 유지하지만, `docker compose down -v`는 DB volume을 삭제하므로 관광 데이터를 다시 import해야 한다.

Docker Compose 실행 시에는 `docker-compose.yml`이 `.env`의 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`을 읽어 백엔드 컨테이너에 주입한다.
실제 `.env` 값은 절대 git에 커밋하지 않는다.

### 확인
```bash
curl localhost:8080/api/health
# {"status":"UP","service":"tripcrew-backend"}
```

### JWT_SECRET 주의사항

- `JWT_SECRET`은 JWT access/refresh token 서명과 검증에 사용한다.
- 회원가입된 사용자 정보와 비밀번호 해시는 DB에 저장되므로 `JWT_SECRET`이 바뀌어도 기존 회원은 다시 로그인할 수 있다.
- 단, 이미 발급된 access/refresh token은 `JWT_SECRET`이 바뀌면 검증 실패한다.
- 로컬 개발 중에는 다시 로그인하면 된다.
- 로그인 유지나 refresh token 재발급 흐름을 테스트할 때는 같은 `JWT_SECRET`을 계속 사용한다.
- 운영/배포 환경에서는 반드시 고정된 강한 시크릿키를 사용한다.
- 실제 `JWT_SECRET`, `DB_PASSWORD` 값은 절대 git에 커밋하지 않는다.

## DB 마이그레이션 (Flyway)

- 앱 기동 시 `classpath:db/migration` 의 `V{버전}__{설명}.sql` 을 버전 순으로 자동 적용한다.
- **한번 적용된 마이그레이션 파일은 절대 수정하지 않는다.** 스키마 변경은 항상 새 파일(`V2__add_xxx.sql`)로 누적한다.
- 적용 이력은 DB의 `flyway_schema_history` 테이블에서 확인할 수 있다.
- 로컬 Docker DB를 초기화해 마이그레이션을 처음부터 다시 적용하려면 `docker compose down -v` 후 재실행한다.

## 자주 쓰는 명령

```bash
./mvnw clean compile      # 컴파일 (DB 불필요)
./mvnw test               # 테스트 (컨텍스트 로딩 시 DB 필요)
./mvnw spring-boot:run    # 실행
./mvnw clean package      # 빌드 (jar)
docker compose up --build # 프론트+백엔드+MySQL 통합 실행
docker compose down       # Docker Compose 중지
```

## 협업 규칙

- `main` 직접 push 금지 — 작업 브랜치 + PR
- 브랜치 이름: `feature/*`, `fix/*`, `docs/*`, `chore/*`
- 커밋 메시지: Conventional Commits 형식 사용
  - 기능 추가: `feat: add JWT authentication`
  - 버그 수정: `fix: add allowPublicKeyRetrieval to JDBC URL`
  - 문서 변경: `docs: update backend local setup guide`
  - 설정/정리: `chore: update attraction schema`

예시:

```bash
git checkout -b docs/backend-local-setup
git add tripcrew-backend/README.md
git commit -m "docs: update backend local setup guide"
git push origin docs/backend-local-setup
```
