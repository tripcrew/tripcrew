# TripCrew 배포 가이드 (AWS EC2 · Docker)

AWS EC2 **t2.micro(프리티어, 1GB)** 에 Docker 로 배포하는 절차.
**개발용 `docker-compose.yml` 과 별개**로 `docker-compose.prod.yml` 을 쓴다.

## 구성 요약
- 외부 노출 포트는 **80(web=Caddy) 하나뿐**. mysql/redis/backend 는 내부 네트워크로만.
- Caddy 가 `/` → 빌드된 프론트(정적), `/api`·`/ws`·`/uploads` → 백엔드로 프록시.
- 프론트·API 가 **같은 origin** → CORS 안 터짐. 프론트 API base 는 상대경로(`/api`).
- DB 데이터(`mysql-data`)·업로드 이미지(`uploads`)는 named 볼륨에 영속.

> ⚠️ **t2.micro 는 RAM 1GB** 라 컨테이너 4개 + 빌드가 메모리 빠듯하다.
> → **swap 필수**(3단계), **JVM/MySQL 메모리 축소**(이미 prod compose·.env 에 반영됨).

---

## 0. 사전 (AWS 콘솔에서 EC2 생성)
- **EC2 → Launch instance**
  - **AMI**: Ubuntu Server 22.04 LTS (프리티어 eligible)
  - **Instance type**: **t2.micro** (프리티어)
  - **Key pair**: 새로 생성 → **`.pem` 파일 다운로드**(이게 있어야 SSH 접속. 분실 시 재접속 불가)
  - **Network settings**: 아래 1단계의 인바운드 규칙을 여기서 바로 설정해도 됨
  - **Storage**: 기본 8GB → **여유 위해 20~30GB** 권장(프리티어 30GB 까지 무료)
- 생성 후 인스턴스의 **Public IPv4 address** 확인 → 이 IP 를 아래에서 사용

## 1. 보안 그룹(Security Group) 인바운드 규칙
EC2 → 해당 인스턴스 → Security → Security Group → **Edit inbound rules**:

| Type        | Protocol | Port | Source            | 용도            |
|-------------|----------|------|-------------------|-----------------|
| SSH         | TCP      | 22   | **My IP**(권장)   | 내 접속         |
| HTTP        | TCP      | 80   | `0.0.0.0/0`       | 웹 공개         |
| (나중에)HTTPS| TCP     | 443  | `0.0.0.0/0`       | HTTPS 전환 시   |

> AWS 는 보안 그룹만 열면 된다. **(Oracle 과 달리 OS 방화벽 iptables 설정 불필요)** — Ubuntu EC2 는 기본적으로 ufw 비활성.

## 2. SSH 접속
```bash
chmod 400 ~/Downloads/<키이름>.pem        # .pem 권한 안 좁히면 SSH 가 거부함
ssh -i ~/Downloads/<키이름>.pem ubuntu@<EC2_퍼블릭IP>
```
(Ubuntu AMI 기본 사용자명은 `ubuntu`)

## 3. swap 추가 (1GB 인스턴스 **필수**)
1GB 로는 4개 컨테이너 + 온박스 빌드가 OOM 난다. 빌드까지 고려해 **4GB** 잡는다.
```bash
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab   # 재부팅 후에도 유지
free -h                                                       # Swap 4Gi 확인
```
> 런타임만 돌릴 거면 2GB 로도 가능하지만, **EC2 에서 직접 빌드**(7단계)하면 4GB 가 안전하다.

## 4. Docker 설치
```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker            # 또는 로그아웃 후 재접속
docker --version         # 확인
```

## 5. 소스 받기
```bash
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/tripcrew/tripcrew.git
cd tripcrew
```

## 6. 환경변수(.env) 작성
```bash
cp .env.prod.example .env
nano .env
```
채울 값:
- `DB_PASSWORD` — 강한 비밀번호
- `JWT_SECRET` — `openssl rand -base64 32` 결과
- `APP_CORS_ORIGINS=http://<EC2_퍼블릭IP>`  ← **본인 서버 IP**, 끝에 슬래시 없이
- `GEMINI_API_KEY` — 발급받은 키
- `JAVA_OPTS=-Xmx384m` — 1GB 기준 그대로 둔다
- (선택) 네이버 지도 키

## 7. 빌드 & 기동 — 방법 A: EC2 에서 직접 빌드 (권장·간단)
```bash
docker compose -f docker-compose.prod.yml up -d --build
```
- t2.micro 는 1 vCPU 라 첫 빌드가 **느리다(10~20분)**. swap 덕에 OOM 없이 완주하면 정상.
- 진행/로그:
```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend   # Flyway 마이그레이션·기동 확인
```

### 방법 B: 로컬에서 빌드 → Docker Hub 푸시 → EC2 에선 pull (빌드가 너무 느리거나 실패할 때)
EC2 빌드가 버겁다면 빌드를 로컬에서 하고 EC2 는 이미지만 받는다.

**로컬(내 PC)에서:**
```bash
# .env 에 IMAGE_PREFIX=<내 DockerHub 사용자명> 설정
docker login
# ⚠️ Apple Silicon(M칩)이면 amd64 로 빌드돼야 EC2(amd64)에서 돈다.
#    prod compose 에 platform: linux/amd64 가 박혀 있어 compose build 가 amd64 로 만든다.
#    (colima 사용 시 buildx/QEMU 필요할 수 있음: `docker buildx version` 확인)
docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml push
```
**EC2 에서:**
```bash
# .env 에 동일한 IMAGE_PREFIX 설정 후
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d        # --build 없이
```

## 8. 동작 확인
```bash
curl http://localhost/api/health        # 서버 안에서
```
브라우저에서 `http://<EC2_퍼블릭IP>` 접속 → 회원가입/로그인까지 되면 성공.

---

## ⚠️ AWS 프리티어 과금 주의 (꼭 읽기)
- **프리티어는 가입 후 12개월 한정.** 이후 t2.micro 도 과금된다(서울 기준 24h 가동 시 월 ~$8 수준).
- **750시간/월** 한도 = t2.micro **1대 24시간 가동**이면 한 달 거의 다 씀. **2대 동시 가동하면 초과 과금**.
- **EBS 스토리지·데이터 전송**도 한도 초과 시 과금. 안 쓰는 인스턴스/볼륨/Elastic IP 는 정리.
  - ⚠️ **Elastic IP 는 인스턴스에 안 붙어 있으면(미사용 상태) 과금**된다. 안 쓰면 release.
- ✅ **Billing Alarm 설정 권장**: AWS Console → **Billing → Budgets**(또는 CloudWatch Billing Alarm)에서
  **월 $1~5 임계치 알림** 걸어두기. 예상 못 한 과금을 메일로 즉시 감지.
- 💡 데모 안 할 땐 **인스턴스 Stop**(중지) 하면 컴퓨트 과금 멈춤(EBS 는 소액 유지). 재시작하면 퍼블릭 IP 가 바뀌니
  고정하려면 Elastic IP 연결(연결 상태면 무료).

---

## 운영 명령 모음
```bash
# 코드 업데이트 후 재배포
git pull
docker compose -f docker-compose.prod.yml up -d --build   # (방법 B면 로컬 build+push → EC2 pull)

# 끄기(데이터 유지)
docker compose -f docker-compose.prod.yml down

# DB 접속
docker compose -f docker-compose.prod.yml exec mysql \
  mysql -uroot -p --default-character-set=utf8mb4 tripcrew
```

---

## 나중에: 도메인 + HTTPS 전환 (OAuth/이메일 인증 전 단계)
무료 도메인(예: DuckDNS) 발급 → A 레코드를 EC2 퍼블릭 IP 로 지정한 뒤:
1. 보안 그룹에 **443** 인바운드 추가(1단계 표 참고)
2. `tripcrew-frontend/Caddyfile` 의 `:80` → `<도메인>` 으로 변경 (Caddy 가 Let's Encrypt 인증서 자동 발급)
3. `docker-compose.prod.yml` 의 `web` 포트에 `"443:443"` 추가
4. `.env` 의 `APP_CORS_ORIGINS=https://<도메인>` 으로 변경
5. `docker compose -f docker-compose.prod.yml up -d --build`

프론트는 상대경로/런타임 origin 을 쓰므로 **재빌드 외 코드 수정 불필요**.
💡 IP 가 바뀌면 도메인 A 레코드도 바뀌니, **Elastic IP 연결**(인스턴스에 붙여두면 무료)로 고정 권장.

---

## 배포 전 보안 하드닝 (공개 시)
- DB 를 root 대신 `tripcrew` DB 권한만 가진 최소권한 계정으로 분리 (현재는 root 접속).
- `git` 히스토리 시크릿 점검(gitleaks) — 과거 `.env.example` 비번 커밋 전례.
- `.env` 권한 `chmod 600`.
- SSH 22 인바운드는 **My IP** 로 제한(0.0.0.0/0 비권장).
