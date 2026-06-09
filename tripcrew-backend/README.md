# TripCrew Backend

TripCrew(여행 계획 공유 플랫폼)의 백엔드 REST API 서버.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Persistence | MyBatis 3.0 · MySQL 8 |
| Migration | Flyway |
| Security | Spring Security · JWT (jjwt) |
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
    │   ├── auth/        # F01 인증 (JWT, 회원가입/로그인)
    │   ├── user/        # users
    │   ├── attraction/  # F02 관광지(외부 API 캐시)
    │   ├── tripplan/    # F03/F04/F06 여행계획·동선·공동편집
    │   ├── review/      # F08 후기/평점
    │   ├── notice/      # F10 공지
    │   └── chat/        # F05 챗봇 (옵션)
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

## 로컬 실행 방법

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

## 자주 쓰는 명령

```bash
./mvnw clean compile      # 컴파일 (DB 불필요)
./mvnw test               # 테스트 (컨텍스트 로딩 시 DB 필요)
./mvnw spring-boot:run    # 실행
./mvnw clean package      # 빌드 (jar)
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
