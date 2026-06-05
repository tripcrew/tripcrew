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

```bash
# 1) MySQL 8 에 DB 생성
mysql -u root -p -e "CREATE DATABASE tripcrew CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2) 환경변수 설정 (~/.zshrc 등 쉘 프로필에 추가 후 source)
export DB_USERNAME=root
export DB_PASSWORD=<본인 DB 비밀번호>
export JWT_SECRET=<base64 인코딩된 256bit 이상 키>   # 인증(F01) 사용 시 필요

# 3) 실행 — 첫 기동 시 Flyway 가 db/migration 의 V1__init.sql 을 자동 적용해
#    9개 테이블을 생성한다.
cd tripcrew-backend
./mvnw spring-boot:run
```

### 확인
```bash
curl localhost:8080/api/health
# {"status":"UP","service":"tripcrew-backend"}
```

`JWT_SECRET` base64 키 생성 예시:
```bash
openssl rand -base64 32
```

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

브랜치/커밋 규칙은 저장소 루트 [`CLAUDE.md`](../CLAUDE.md) 를 따른다.
- `main` 직접 push 금지 — 작업 브랜치 + PR
- 브랜치: `feature/*`, `fix/*`, `docs/*`
- 커밋: Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:` ...)
