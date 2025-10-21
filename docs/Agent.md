## Agent.md (Best Practice Version for Codex)

> **Title**: Firebase 인증 기반 To-Do 리스트 서비스 (Spring Boot 3.3.3 + Java 21 + Next.js 14 + PostgreSQL)
>
> **Updated**: 2025-10-21
>
> 이 문서는 Codex가 **이 파일만 읽고도** 작업을 정확히 수행할 수 있도록 작성된 실행 명세서입니다. (Single Source of Truth)

---

### 0. 목적 및 범위

- 로그인 사용자만 접근 가능한 **개인별 To-Do 리스트 서비스**
- 기능: CRUD + 완료 상태 필터링
- 비기능 목표: 로그인 성공율 ≥ 99.5%, API p95 < 200ms
- 비포함: 오프라인 기능, 공유, 모바일 앱

---

### 1. 기술 스택 및 아키텍처

| 구성요소     | 기술 선택                                              |
| ------------ | ------------------------------------------------------ |
| 프론트엔드   | Next.js 14 (App Router) + TypeScript + React Query     |
| 인증         | Firebase Authentication (Email/Password, Google Login) |
| 백엔드       | Java 21 + Spring Boot 3.3.3 + Hexagonal Architecture   |
| 데이터베이스 | PostgreSQL 14 (Docker), Spring Data JPA, Flyway        |
| 테스트       | JUnit 5.10, Testcontainers(Postgres), Playwright (e2e) |
| 빌드         | Gradle 8.10+, GitHub Actions CI                        |
| 컨테이너     | Dockerfile(api/web), docker-compose.yml 포함           |

> 참고: `javax.*` → `jakarta.*` 네임스페이스 변경

---

### 2. 프로젝트 구조 (Monorepo)

```plaintext
/
├── api/           # Spring Boot 백엔드
├── web/           # Next.js 프론트엔드
├── infra/         # Docker, Compose, Firebase 인증 키
├── docs/          # 문서 및 명세
├── Makefile       # 통합 빌드/실행 스크립트
└── codex.config.yaml
```

---

### 3. API 설계 요약

- Prefix: `/api`, 인증: `Bearer Firebase ID Token`
- 상태 코드: 200/201/204/401/403/422/500
- 엔드포인트:
  - `GET    /todos?status=all|active|done`
  - `POST   /todos`
  - `PATCH  /todos/{id}`
  - `DELETE /todos/{id}`

---

### 4. 도메인 모델 및 검증

```ts
interface Todo {
  id: UUID;
  userId: string; // Firebase UID
  title: string;  // 1~100자
  description?: string;
  dueDate?: ISODate;
  done: boolean;  // 기본값 false
  createdAt: UTCDate;
  updatedAt: UTCDate;
}
```

- 검증 규칙:
  - title 필수, 길이 제한
  - userId로 스코프 격리
  - 서버 시간 기준 UTC 저장

---

### 5. Codex Task 블록

#### `TASK:INIT-REPO`

- Java 21 + Gradle 8.10 백엔드, Node 20 기반 프론트 scaffolding 생성
- Dockerfile(api/web), docker-compose.yml, Makefile 포함
- Testcontainers 기반 테스트 가능하도록 구성

#### `TASK:API-TODO`

- CRUD 엔드포인트 + Firebase ID Token 필터
- JPA Repository + DTO + Entity 분리
- Flyway로 스키마 마이그레이션
- 단위/통합 테스트 포함 (Testcontainers 활용)

#### `TASK:UI-TODO`

- 로그인 후 `/todos` 화면 구성
- 상태 필터, 낙관적 업데이트
- React Query 기반 API 요청 관리
- e2e 테스트 작성 (Playwright)

#### `TASK:CI`

- GitHub Actions: Node 20, Java 21, Gradle 8.x 캐시 및 테스트 워크플로우 작성

---

### 6. 실행 및 환경 구성

```bash
make up         # 전체 docker-compose 실행
make down       # 정리
make api        # 백엔드 실행 (로컬)
make web        # 프론트 실행 (로컬)
make test-api   # 백엔드 테스트
make test-web   # 프론트 유닛 + e2e
```

#### 환경 변수 예시

- `api/.env`
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/todo`
  - `FIREBASE_CREDENTIALS_PATH=infra/firebase/service-account.json`
- `web/.env`
  - `NEXT_PUBLIC_FIREBASE_API_KEY=...`
  - `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=...`

---

### 7. 테스트 전략

- 단위: 도메인 로직, Validator, Repository Mock
- 통합: Spring + Testcontainers (Postgres)
- e2e: 로그인 → 추가 → 완료 → 필터 → 삭제 (Playwright)

---

### 8. Codex 주의 사항

- 이 Agent.md만으로 모든 Codex 작업 가능해야 함
- 불분명한 요구사항은 `/docs/decisions/ADR-*.md`에 기록
- 외부 장애 시 mock 또는 가정으로 우회 진행
