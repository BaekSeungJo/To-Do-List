## PRD.md (Codex 최적화 버전)

> **프로젝트**: 로그인 기반 개인용 To-Do 서비스  
> **버전**: 1.0  
> **작성일**: 2025-10-21  
> **관련 문서**: [Agent.md](./Agent.md)

---

### 1. 개요

**목표**: Firebase 계정 기반 인증을 통해 사용자가 개인 To-Do 항목을 안전하게 관리할 수 있는 웹 서비스를 제공한다.

- 로그인: Firebase Auth (Email/Password, Google)
- 할 일 관리: CRUD + 상태 필터 (전체/완료/미완료)
- 멀티 디바이스 대응: 모바일 브라우저 포함
- 성능 목표: p95 응답시간 < 200ms, 로그인 성공율 ≥ 99.5%

---

### 2. 핵심 기능 요구사항

| ID    | 기능            | 설명                                   | 우선순위 |
| ----- | --------------- | -------------------------------------- | -------- |
| F-001 | 로그인/로그아웃 | Firebase 인증 (Email/Password, Google) | ★★★★     |
| F-002 | To-Do 생성      | 제목/기한 입력, 초기 상태 done=false   | ★★★★     |
| F-003 | To-Do 목록 조회 | 필터(전체/완료/미완료), 생성일 역순    | ★★★      |
| F-004 | To-Do 수정      | 제목, 기한, 상태 변경                  | ★★★      |
| F-005 | To-Do 삭제      | 단건 삭제, 하드 딜리트                 | ★★       |
| F-006 | 반응형 UI       | 모바일/데스크탑 대응                   | ★★       |
| F-007 | 인증 만료 처리  | Firebase 토큰 만료 시 자동 로그아웃    | ★★       |

---

### 3. 사용자 시나리오

**Persona: 일반 사용자**

- "나는 내 계정으로 로그인해서 오늘의 할 일을 적고 체크하고 싶다."

**행동 흐름:**

1. Firebase 로그인
2. 할 일 추가 (제목/기한)
3. 완료 여부 체크
4. 전체/완료/미완료 필터링
5. 할 일 삭제

**예외 처리:**

- 인증 실패 시 에러 표시 및 재시도
- 제목 미입력 시 저장 차단
- 토큰 만료 시 자동 로그아웃 후 로그인 페이지로 이동

---

### 4. 데이터 모델 (개념 모델)

**Todo Entity**

| 필드      | 타입     | 설명                     |
| --------- | -------- | ------------------------ |
| id        | UUID     | 고유 식별자              |
| userId    | string   | Firebase UID             |
| title     | string   | 1~100자                  |
| dueDate   | datetime | 선택적 마감일            |
| done      | boolean  | 완료 여부 (기본값 false) |
| createdAt | datetime | 생성 시각 (UTC)          |
| updatedAt | datetime | 수정 시각 (UTC)          |

---

### 5. API 설계 (요약)

| 동작      | 요청                     | 응답           |
| --------- | ------------------------ | -------------- |
| 로그인    | Firebase SDK 사용        | ID Token       |
| 목록 조회 | `GET /api/todos`         | 200 OK / List  |
| 생성      | `POST /api/todos`        | 201 Created    |
| 수정      | `PATCH /api/todos/{id}`  | 200 OK         |
| 삭제      | `DELETE /api/todos/{id}` | 204 No Content |

> 모든 API는 Bearer Firebase ID Token 기반 인증 필요

---

### 6. 비기능 요구사항 (NFR)

| 항목     | 기준                          |
| -------- | ----------------------------- |
| 인증     | Firebase ID Token 검증        |
| 성능     | 평균 응답시간 < 100ms         |
| 보안     | HTTPS 통신, 토큰 노출 방지    |
| 안정성   | DB 복구 시간 5분 이내         |
| 유지보수 | 신규 기능 추가 시 3일 내 배포 |
| 접근성   | ARIA 준수 (WCAG AA)           |

---

### 7. 성공 조건 (Acceptance Criteria)

- AC-001: 로그인 후 `/todos` 접근 가능
- AC-002: 인증 없는 사용자는 401 반환
- AC-003: To-Do 생성 후 목록에 바로 반영됨
- AC-004: 완료 토글 시 필터와 상태 일치
- AC-005: 전체 e2e 시나리오 통과

---

### 8. 제약 및 가정

| 항목            | 내용                         |
| --------------- | ---------------------------- |
| 동시 사용자     | 100명 내외                   |
| 사용자당 데이터 | 약 500건 이내 예상           |
| 네트워크        | HTTPS 환경, 프록시 없음      |
| 브라우저        | 최신 Chrome/Edge/Safari 지원 |
| 운영 체제       | 모바일 + 데스크탑 웹브라우저 |

---

### 9. Codex 실행 예시

```bash
# 초기 설정
codex run --context docs/PRD.md --agent docs/Agent.md --task INIT-REPO

# API 구현
codex run --context docs/PRD.md --agent docs/Agent.md --task API-TODO

# UI 구현
codex run --context docs/PRD.md --agent docs/Agent.md --task UI-TODO
```

> PRD.md는 "왜 & 무엇"을 정의하고, Agent.md는 "어떻게 & 어디에"를 명시합니다. 두 문서는 항상 동기화되어야 하며, 변경 시 `docs/changelogs/PRD_CHANGELOG.md`에 기록합니다.
