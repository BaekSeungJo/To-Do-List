## PRD.md (Codex용 간결 버전)

> **프로젝트명:** Firebase 로그인 기반 개인용 To-Do 서비스  
> **버전:** 1.1  
> **작성일:** 2025-10-22  
> **참조 문서:** [Agent.md](./Agent.md)

---

### 1. 개요

Firebase 계정 인증을 통해 사용자가 자신의 To-Do 항목을 안전하게 관리할 수 있는 웹 서비스.

- 로그인: Firebase Auth (Email/Password, Google)
- 할 일 관리: CRUD + 상태 필터 (전체/완료/미완료)
- 반응형 UI: 모바일/데스크탑 대응
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
| F-007 | 인증 만료 처리  | 토큰 만료 시 자동 로그아웃             | ★★       |

---

### 3. 사용자 시나리오

**Persona:** 로그인 기반 개인 사용자  
**목표:** “로그인 후 내 할 일을 작성하고, 완료 여부를 손쉽게 관리하고 싶다.”

**행동 흐름**

1. Firebase로 로그인
2. 새 할 일 추가 (제목/기한)
3. 완료 체크 또는 해제
4. 필터(전체/완료/미완료)로 상태별 보기
5. 필요 시 삭제

**예외 처리**

- 인증 실패 → 오류 메시지 및 재시도
- 제목 미입력 → 저장 불가
- 토큰 만료 → 자동 로그아웃 후 로그인 페이지 이동

---

### 4. 데이터 모델 (개념 수준)

| 필드명    | 타입     | 설명                   |
| --------- | -------- | ---------------------- |
| id        | UUID     | 고유 식별자            |
| userId    | string   | Firebase UID           |
| title     | string   | 1~100자                |
| dueDate   | datetime | 선택적 마감일          |
| done      | boolean  | 완료 여부 (기본 false) |
| createdAt | datetime | 생성 시각 (UTC)        |
| updatedAt | datetime | 수정 시각 (UTC)        |

> 상세 타입 및 검증 규칙은 [Agent.md](./Agent.md)의 “도메인 모델 및 검증” 참고

---

### 5. API 요약

| 동작      | 메서드/경로              | 요약 설명                              |
| --------- | ------------------------ | -------------------------------------- |
| 로그인    | Firebase SDK             | Firebase 인증 (Email/Password, Google) |
| 목록 조회 | `GET /api/todos`         | 전체/필터별 할 일 조회                 |
| 생성      | `POST /api/todos`        | 새 할 일 추가                          |
| 수정      | `PATCH /api/todos/{id}`  | 제목·기한·상태 변경                    |
| 삭제      | `DELETE /api/todos/{id}` | 할 일 삭제                             |

> 모든 API는 Bearer Firebase ID Token 기반 인증 필요  
> 상태코드·Request/Response 스펙은 [Agent.md](./Agent.md) 참조

---

### 6. 비기능 요구사항 (NFR)

| 항목     | 기준                          |
| -------- | ----------------------------- |
| 인증     | Firebase ID Token 검증        |
| 보안     | HTTPS 통신, 토큰 노출 방지    |
| 성능     | 평균 응답시간 < 100ms         |
| 안정성   | DB 복구 시간 5분 이내         |
| 유지보수 | 신규 기능 추가 시 3일 내 배포 |
| 접근성   | ARIA 준수 (WCAG AA)           |

---

### 7. 성공 조건 (Acceptance Criteria)

| ID     | 조건                         |
| ------ | ---------------------------- |
| AC-001 | 로그인 후 `/todos` 접근 가능 |
| AC-002 | 인증 없는 사용자는 401 반환  |
| AC-003 | To-Do 생성 시 목록 즉시 반영 |
| AC-004 | 완료 토글 시 상태·필터 일치  |
| AC-005 | 전체 e2e 시나리오 통과       |

---

### 8. 제약 및 가정

| 항목            | 내용                     |
| --------------- | ------------------------ |
| 동시 사용자     | 약 100명 내외            |
| 사용자당 데이터 | 500건 이내               |
| 네트워크        | HTTPS 환경, 프록시 없음  |
| 브라우저        | 최신 Chrome/Edge/Safari  |
| 운영체제        | 모바일/데스크탑 브라우저 |

---

### 9. Codex 실행 예시

```bash
# 초기 세팅
codex run --context docs/PRD.md --agent docs/Agent.md --task INIT-REPO

# API 구현
codex run --context docs/PRD.md --agent docs/Agent.md --task API-TODO

# UI 구현
codex run --context docs/PRD.md --agent docs/Agent.md --task UI-TODO
```
