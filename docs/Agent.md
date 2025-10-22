## Agent.md (Codex용 간결 버전)

> **프로젝트명:** Firebase 인증 기반 To-Do 리스트 서비스  
> **버전:** 1.1  
> **업데이트:** 2025-10-22  
> **목적:** Codex가 이 문서만으로 백엔드·프론트엔드 코드를 자동 생성할 수 있도록 구체적 구현 지침을 제공

---

### 0. 목적 및 범위

- 로그인 사용자 전용 **개인별 To-Do 관리 서비스**
- 기능: CRUD + 완료 상태 필터링
- 비포함: 공유, 오프라인 동기화, 모바일 앱
- 비기능 목표: 로그인 성공율 ≥ 99.5%, API p95 < 200ms

---

### 1. 기술 스택 및 아키텍처

| 구성 요소    | 기술 스택                                               |
| ------------ | ------------------------------------------------------- |
| 프론트엔드   | Next.js 14 (App Router) + TypeScript + React Query      |
| 인증         | Firebase Authentication (Email/Password, Google Login)  |
| 백엔드       | Java 21 + Spring Boot 3.3.3 + Hexagonal Architecture    |
| 데이터베이스 | PostgreSQL 14 (Docker) + Spring Data JPA + Flyway       |
| 테스트       | JUnit 5.10 + Testcontainers(Postgres) + Playwright(e2e) |
| 빌드/CI      | Gradle 8.10+ + GitHub Actions                           |
| 컨테이너     | Dockerfile(api/web) + docker-compose.yml + Makefile     |

---

### 2. 프로젝트 구조 (Monorepo)

```plaintext
/
├── api/           # Spring Boot 백엔드
├── web/           # Next.js 프론트엔드
├── infra/         # Docker, Compose, Firebase 인증 키
├── docs/          # 명세 문서
└── Makefile       # 통합 빌드/실행 스크립트
```

---

### 3. API 설계

- Prefix: `/api`
- 인증: `Bearer Firebase ID Token`
- 공통 상태 코드: 200, 201, 204, 401, 403, 422, 500

| 메서드 | 경로                                | 설명                |
| ------ | ----------------------------------- | ------------------- |
| GET    | `/api/todos?status=all|active|done` | 필터별 To-Do 조회   |
| POST   | `/api/todos`                        | 새 To-Do 추가       |
| PATCH  | `/api/todos/{id}`                   | 제목/기한/상태 변경 |
| DELETE | `/api/todos/{id}`                   | 할 일 삭제          |

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

**검증 규칙**

- `title`: 필수, 길이 1~100자
- `userId`: 인증 사용자 UID와 일치해야 함
- `done`: 기본값 false
- 시간 필드는 모두 UTC 기준

---

### 5. Codex Task 블록

#### `TASK:INIT-REPO`

- Java 21 + Gradle 8.10 기반 백엔드, Node 20 기반 프론트엔드 초기화
- Dockerfile(api/web), docker-compose.yml, Makefile 생성
- Testcontainers 기반 통합 테스트 설정

#### `TASK:API-TODO`

- Firebase ID Token 인증 필터 추가
- CRUD 엔드포인트 구현 (`GET/POST/PATCH/DELETE /api/todos`)
- Entity, DTO, Repository, Service, Controller 구조 적용
- Flyway 마이그레이션 스크립트 작성
- 단위/통합 테스트 포함 (Testcontainers 활용)

#### `TASK:UI-TODO`

- 로그인 후 `/todos` 페이지 구성
- 상태 필터 및 낙관적 업데이트 적용
- React Query로 API 연동
- e2e 테스트 (Playwright) 작성

#### `TASK:CI`

- GitHub Actions 워크플로우:
  - Node 20 / Java 21 환경
  - Gradle 빌드 및 테스트 캐싱
  - PR 시 자동 테스트 수행

---

### 6. 실행 및 환경 구성

```bash
make up         # docker-compose 전체 실행
make down       # 종료 및 정리
make api        # 백엔드 로컬 실행
make web        # 프론트 로컬 실행
make test-api   # 백엔드 테스트
make test-web   # 프론트 테스트 (unit + e2e)
```

**환경 변수 예시**

- `api/.env`
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/todo`
  - `FIREBASE_CREDENTIALS_PATH=infra/firebase/service-account.json`
- `web/.env`
  - `NEXT_PUBLIC_FIREBASE_API_KEY=...`
  - `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=...`

---

### 7. 테스트 전략

| 구분              | 도구           | 내용                                        |
| ----------------- | -------------- | ------------------------------------------- |
| 단위(Unit)        | JUnit 5        | 도메인 로직 및 Validator 테스트             |
| 통합(Integration) | Testcontainers | DB 연동 테스트(PostgreSQL)                  |
| e2e               | Playwright     | 로그인 → 생성 → 완료 → 필터 → 삭제 시나리오 |

---

### 8. Codex 주의사항

- Codex는 **이 문서만으로 전체 코드를 생성할 수 있어야 함**
- 불명확한 요구사항은 `/docs/decisions/ADR-*.md`에 기록
- 외부 API 장애 시 Mock/Fallback 처리
- PRD.md에 정의된 기능/비기능 요구사항을 항상 기준으로 삼을 것

---
