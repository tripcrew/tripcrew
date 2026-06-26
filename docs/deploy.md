# TripCrew 배포 가이드 (무료 VM · Docker)

Oracle Cloud(또는 임의의 Ubuntu VM)에 Docker 로 배포하는 절차.
**개발용 `docker-compose.yml` 과 별개**로 `docker-compose.prod.yml` 을 쓴다.

## 구성 요약
- 외부 노출 포트는 **80(web=Caddy) 하나뿐**. mysql/redis/backend 는 내부 네트워크로만.
- Caddy 가 `/` → 빌드된 프론트(정적), `/api`·`/ws`·`/uploads` → 백엔드로 프록시.
- 프론트·API 가 **같은 origin** → CORS 안 터짐. 프론트 API base 는 상대경로(`/api`).
- DB 데이터(`mysql-data`)·업로드 이미지(`uploads`)는 named 볼륨에 영속.

---

## 0. 사전 (VM 준비 완료 가정)
- Ubuntu 22.04 VM, SSH 접속 가능
- Oracle 콘솔 **Security List(VCN)** 에서 Ingress TCP **80** 개방 (`0.0.0.0/0`)

## 1. SSH 접속
```bash
ssh -i <받은_private_key> ubuntu@<VM_공인IP>
```

## 2. OS 방화벽에서 80 포트 열기 (Oracle Ubuntu 필수 — 자주 놓침)
Oracle Ubuntu 이미지는 iptables 가 기본 차단한다. Security List(1단계) 만으로는 부족.
```bash
sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
sudo netfilter-persistent save
# (HTTPS 전환 시) sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT && sudo netfilter-persistent save
```

## 3. Docker 설치
```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker            # 또는 로그아웃 후 재접속
docker --version         # 확인
```

## 4. (메모리 1~2GB 작은 인스턴스만) swap 추가 — 선택
24GB ARM 이면 건너뛴다. 1GB 인스턴스면 빌드/구동 OOM 방지용:
```bash
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
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
- `APP_CORS_ORIGINS=http://<VM_공인IP>`  ← **본인 서버 IP**, 끝에 슬래시 없이
- `GEMINI_API_KEY` — 발급받은 키
- (선택) 네이버 지도 키

## 7. 빌드 & 기동
```bash
docker compose -f docker-compose.prod.yml up -d --build
```
첫 빌드는 Maven 의존성/프론트 빌드로 몇 분 걸린다. 진행/로그:
```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend   # Flyway 마이그레이션·기동 확인 (Ctrl+C 로 빠져나옴)
```

## 8. 동작 확인
```bash
curl http://localhost/api/health        # 서버 안에서
```
브라우저에서 `http://<VM_공인IP>` 접속 → 회원가입/로그인까지 되면 성공.

---

## 운영 명령 모음
```bash
# 코드 업데이트 후 재배포
git pull
docker compose -f docker-compose.prod.yml up -d --build

# 끄기(데이터 유지)
docker compose -f docker-compose.prod.yml down

# DB 접속
docker compose -f docker-compose.prod.yml exec mysql \
  mysql -uroot -p --default-character-set=utf8mb4 tripcrew
```

---

## 나중에: 도메인 + HTTPS 전환 (OAuth/이메일 인증 전 단계)
무료 도메인(예: DuckDNS) 발급 → A 레코드를 VM IP 로 지정한 뒤:
1. OS 방화벽 443 개방 (2단계 참고)
2. Oracle Security List 에 443 Ingress 추가
3. `tripcrew-frontend/Caddyfile` 의 `:80` → `<도메인>` 으로 변경 (Caddy 가 Let's Encrypt 인증서 자동 발급)
4. `docker-compose.prod.yml` 의 `web` 포트에 `"443:443"` 추가
5. `.env` 의 `APP_CORS_ORIGINS=https://<도메인>` 으로 변경
6. `docker compose -f docker-compose.prod.yml up -d --build`

프론트는 상대경로/런타임 origin 을 쓰므로 **재빌드 외 코드 수정 불필요**.

---

## 배포 전 보안 하드닝 (공개 시)
- DB 를 root 대신 `tripcrew` DB 권한만 가진 최소권한 계정으로 분리 (현재는 root 접속).
- `git` 히스토리 시크릿 점검(gitleaks) — 과거 `.env.example` 비번 커밋 전례.
- `.env` 권한 `chmod 600`.
