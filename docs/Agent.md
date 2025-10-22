## Agent.md (Codex용 간결 버전)

> **프로젝트명:** Firebase 인증 기반 To-Do 리스트 서비스  
> **버전:** 1.3  
> **업데이트:** 2025-10-22  
> **목적:** Codex가 이 문서만으로 백엔드·프론트엔드 코드를 자동 생성할 수 있도록 **헥사고날(Ports & Adapters)** + **DDD** 설계 조건을 포함한 구현 지침 제공

---

### 0. 목적 및 범위

- 로그인 사용자 전용 **개인별 To-Do 관리 서비스**
- 기능: CRUD + 완료 상태 필터링
- 비포함: 공유, 오프라인 동기화, 모바일 앱
- 비기능 목표: 로그인 성공율 ≥ 99.5%, API p95 < 200ms

---

### 1. 기술 스택 및 아키텍처

| 구성 요소    | 기술 스택                                                    |
| ------------ | ------------------------------------------------------------ |
| 프론트엔드   | Next.js 14 (App Router) + TypeScript + React Query           |
| 인증         | Firebase Authentication (Email/Password, Google Login)       |
| 백엔드       | **Java 21 + Spring Boot 3.3.3 + Hexagonal Architecture + DDD** |
| 데이터베이스 | PostgreSQL 14 (Docker) + Spring Data JPA + Flyway            |
| 테스트       | JUnit 5.10 + Testcontainers(Postgres) + Playwright(e2e)      |
| 빌드/CI      | Gradle 8.10+ + GitHub Actions                                |
| 컨테이너     | Dockerfile(api/web) + docker-compose.yml + Makefile          |

---

### 2. 프로젝트 구조 (Monorepo + Hexagonal 패키지)

```plaintext
/
├── api/           # Spring Boot 백엔드 (Hexagonal + DDD)
│   ├── src/main/java/com/example/todo
│   │   ├── application/                 # UseCases, Services (유스케이스 조합/흐름)
│   │   │   ├── command|query/           # 커맨드/쿼리 모델
│   │   │   ├── port/in/                 # Inbound Ports (유스케이스 인터페이스)
│   │   │   ├── port/out/                # Outbound Ports (저장소/외부연동 인터페이스)
│   │   │   └── service/                 # 유스케이스 구현체(의존: port, domain)
│   │   ├── domain/                      # 엔티티, 값객체, 도메인 서비스, 이벤트
│   │   │   ├── model/                   # Aggregate/Entity/ValueObject
│   │   │   ├── event/                   # Domain Events
│   │   │   ├── policy/                  # 도메인 정책/규칙, Specification
│   │   │   └── service/                 # 순수 도메인 서비스
│   │   ├── adapter/
│   │   │   ├── in/web/                  # REST Controller (DTO ↔ Command/Query 변환)
│   │   │   ├── out/persistence/         # JPA Adapter (Repository 구현)
│   │   │   └── out/auth/                # Firebase Auth Adapter (ACL)
│   │   ├── config/                      # Bean 구성, Security Filter, ArchUnit(선택)
│   │   └── common/                      # 공통 예외/에러핸들러/유틸
│   └── build.gradle
├── web/           # Next.js 프론트엔드
├── infra/         # Docker, Compose, Firebase 인증 키
├── docs/          # 명세 문서
└── Makefile       # 통합 빌드/실행 스크립트
```

---

## 3. DDD 설계 지침 (API 중심)

### 3.1 전략적 설계 (Strategic Design)

- **Bounded Context:** `TodoManagement` (본 서비스 전부를 1개 컨텍스트로 단순화)
- **Ubiquitous Language:** _Todo(할 일), 완료(done), 기한(dueDate), 소유자(userId)_ 등 용어를 코드/문서/테스트에서 동일하게 사용
- **Context Map:** 외부 시스템(Firebase Auth)은 **ACL(Anti-Corruption Layer)** 로 격리 (adapter.out.auth)

### 3.2 전술적 설계 (Tactical Design)

- **Aggregate Root:** `Todo` (소유자 `userId` 기준으로 스코프 고립)
- **Entity:** `Todo` (식별자 `UUID`), 변경 시 **불변 객체 재생성 패턴** 권장
- **Value Object:** `Title(1~100자)`, `DueDate(과거 허용/불허 정책 선택 가능)`, `UserId(Firebase UID)`
- **Repository (Port):** `LoadTodoPort`, `SaveTodoPort`, `UpdateTodoPort`, `DeleteTodoPort`
- **Domain Services:** 복수 엔티티 규칙이 필요한 경우에만 사용 (초기엔 불필요할 수 있음)
- **Domain Events:** `TodoCreated`, `TodoCompletedToggled` 등 (필요 시 Outbox로 발행)

### 3.3 도메인 규칙 (Invariants)

- `Title` 은 공백 제외 1~100자
- `userId` 는 인증된 사용자와 일치해야 함
- `done` 토글 시 `updatedAt` 갱신
- 생성/수정 시각은 **서버 Clock** 기준(UTC); `Clock` 주입으로 테스트 가능

### 3.4 명령/쿼리 (Application 계층 API 표준)

- **Command:** 외부 입력을 내부 도메인으로 바꾸는 의도를 표현 (`CreateTodoCommand`, `ToggleDoneCommand`, `UpdateTodoCommand`)
- **Query:** 조회 의도 (`GetTodosQuery` with `status=ALL|ACTIVE|DONE`, pagination)

```java
// application/port/in
public interface CreateTodoUseCase {
    TodoResult create(CreateTodoCommand command, AuthenticatedUser user);
}
public record CreateTodoCommand(String title, LocalDate dueDate) {}
```

### 3.5 도메인 모델 예시

```java
// domain/model
public final class Todo {
    private final UUID id;
    private final UserId userId;
    private final Title title;
    private final DueDate dueDate;
    private final boolean done;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Todo(...) { ... }

    public static Todo create(UserId userId, Title title, DueDate dueDate, Instant now) {
        return new Todo(UUID.randomUUID(), userId, title, dueDate, false, now, now);
    }

    public Todo toggleDone(Instant now) {
        return new Todo(id, userId, title, dueDate, !done, createdAt, now);
    }
}
```

### 3.6 Domain Event (선택)

```java
// domain/event
public record TodoCreated(UUID todoId, String userId, Instant occurredAt) {}
public record TodoDoneToggled(UUID todoId, boolean done, Instant occurredAt) {}
```

### 3.7 Anti-Corruption Layer (Firebase → Domain)

- `FirebaseAuthAdapter` 가 Firebase 토큰 검증 후 **도메인 값객체 `UserId`** 로 변환
- 애플리케이션 계층에는 외부 SDK 타입을 노출하지 않음

---

## 4. Hexagonal Architecture 설계 조건 (보강)

1) **의존성 규칙**

- `adapter` → `application` / `domain` 은 의존 가능, 역방향 금지
- `application` → `domain`, `port` 인터페이스만 의존 (구현체 모름)
- `domain` 은 어떤 프레임워크에도 의존 금지 (순수 자바)

2) **경계 및 모델 변환**

- Controller DTO ↔ **Command/Query** ↔ Domain ↔ Result DTO
- 트랜잭션 경계: 유스케이스 구현(Service)에서 시작 (`@Transactional`)

3) **명명 규칙**

- Inbound Port: `XxxUseCase` (예: `CreateTodoUseCase`)
- Outbound Port: `XxxPort` (예: `LoadTodoPort`, `SaveTodoPort`)
- Adapter: `XxxAdapter` (예: `TodoPersistenceAdapter`, `FirebaseAuthAdapter`)

4) **예외 매핑 표준**

- 401 `UnauthorizedException`, 403 `ForbiddenException`, 404 `NotFoundException`, 422 `ValidationException`, 500 기타
- `@RestControllerAdvice` 로 에러 응답 표준화(추적ID 포함)

5) **아키텍처 규칙 자동화(선택)**

- ArchUnit로 `controller→repository 직접 의존 금지` 등 검사
- CI에 ArchUnit 테스트 포함

---

### 5. API 설계 (불변)

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

### 6. 도메인 모델 스펙 (요약)

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

**검증 규칙 요약**

- `title`: 1~100자, 공백 제외
- `userId`: 인증 사용자 UID와 일치
- 시간: UTC, `Clock` 주입

---

### 7. Codex Task 블록 (Hexagonal + DDD 반영)

#### `TASK:INIT-REPO`

- 패키지/모듈 골격을 **Hexagonal + DDD** 규칙대로 생성 (위 2장·3장 구조)
- Dockerfile(api/web), docker-compose.yml, Makefile 생성
- Testcontainers(Postgres) 기본 통합 테스트 셋업
- (선택) ArchUnit 규칙 테스트 추가

#### `TASK:API-TODO`

- Firebase 인증 필터 + `AuthenticatedUser` 리졸버 구현
- **Commands/Queries** + **Ports** 정의 (`Create/Get/Update/Delete*UseCase`, `Load/Save/Update/Delete*Port`)
- 유스케이스 구현: `application.service.*` (트랜잭션 시작)
- 도메인: `Todo`(Aggregate), `Title/UserId/DueDate`(Value Objects), **Domain Events(선택)**
- JPA 어댑터 + Flyway DDL(`todos` 테이블)
- 컨트롤러: DTO ↔ Command/Result 변환, 예외 매핑
- 테스트: 도메인(Unit) → 애플리케이션(Slice) → 어댑터(통합) → e2e

#### `TASK:UI-TODO`

- 로그인 후 `/todos` 화면 구성
- 상태 필터 및 낙관적 업데이트 적용
- React Query로 API 연동
- e2e 테스트 (Playwright)

#### `TASK:CI`

- GitHub Actions: Node 20 / Java 21 / Gradle 캐시
- 유닛/통합/e2e + (선택) ArchUnit 검사

---

### 8. 실행 및 환경 구성

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

### 9. 테스트 전략

| 구분                | 도구              | 내용                               |
| ------------------- | ----------------- | ---------------------------------- |
| 도메인(Unit)        | JUnit 5           | 도메인 규칙·행위 테스트            |
| 애플리케이션(Slice) | JUnit 5 + Mockito | 유스케이스 포트 목킹               |
| 어댑터(통합)        | Testcontainers    | JPA 어댑터 + Flyway                |
| e2e                 | Playwright        | 로그인 → 생성 → 완료 → 필터 → 삭제 |

---

### 10. Codex 주의사항

- Codex는 **이 문서만으로 전체 코드를 생성할 수 있어야 함**
- PRD.md의 기능/비기능 요구사항을 기준으로 구현
- **Hexagonal + DDD 규칙 위반 금지** (ArchUnit로 검증 가능)
- 외부 API 장애 시 Mock/Fallback 처리
- 모든 시간 필드는 UTC 저장

---

### 11. ArchUnit 규칙 샘플 (JUnit 5)

아래 예시는 **Hexagonal + DDD** 규칙을 자동 검증하는 테스트 스위트입니다.  
패키지 기준은 본 문서 2장(프로젝트 구조)에 제시한 경로를 따릅니다.

#### 11.1 Gradle 의존성 (api/build.gradle)

```groovy
dependencies {
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
}
```

#### 11.2 패키지 명세 상수

```java
// src/test/java/com/example/todo/architecture/PackageRules.java
package com.example.todo.architecture;

public final class PackageRules {
    private PackageRules() {}

    // 루트
    public static final String ROOT = "com.example.todo..";

    // Hexagonal 패키지들
    public static final String DOMAIN = "com.example.todo.domain..";
    public static final String APPLICATION = "com.example.todo.application..";
    public static final String APPLICATION_SERVICE = "com.example.todo.application.service..";
    public static final String APPLICATION_PORT_IN = "com.example.todo.application.port.in..";
    public static final String APPLICATION_PORT_OUT = "com.example.todo.application.port.out..";
    public static final String ADAPTER_IN_WEB = "com.example.todo.adapter.in.web..";
    public static final String ADAPTER_OUT_PERSISTENCE = "com.example.todo.adapter.out.persistence..";
    public static final String ADAPTER_OUT_AUTH = "com.example.todo.adapter.out.auth..";
    public static final String CONFIG = "com.example.todo.config..";
    public static final String COMMON = "com.example.todo.common..";
}
```

#### 11.3 레이어링 규칙 (의존 방향)

```java
// src/test/java/com/example/todo/architecture/LayeringRulesTest.java
package com.example.todo.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.example.todo")
public class LayeringRulesTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_frameworks =
        noClasses().that().resideInAnyPackage(PackageRules.DOMAIN)
                   .should().dependOnClassesThat().resideInAnyPackage(
                       "org.springframework..",
                       "jakarta..",
                       "javax..",
                       "org.hibernate.."
                   );

    @ArchTest
    static final ArchRule application_should_not_depend_on_adapters =
        noClasses().that().resideInAnyPackage(PackageRules.APPLICATION)
                   .should().dependOnClassesThat().resideInAnyPackage(
                       PackageRules.ADAPTER_IN_WEB,
                       PackageRules.ADAPTER_OUT_PERSISTENCE,
                       PackageRules.ADAPTER_OUT_AUTH
                   );

    @ArchTest
    static final ArchRule web_adapter_should_not_depend_on_persistence_adapter =
        noClasses().that().resideInAnyPackage(PackageRules.ADAPTER_IN_WEB)
                   .should().dependOnClassesThat().resideInAnyPackage(PackageRules.ADAPTER_OUT_PERSISTENCE);

    @ArchTest
    static final ArchRule persistence_adapter_should_not_depend_on_application_service_impl =
        noClasses().that().resideInAnyPackage(PackageRules.ADAPTER_OUT_PERSISTENCE)
                   .should().dependOnClassesThat().resideInAnyPackage(PackageRules.APPLICATION_SERVICE);
}
```

#### 11.4 포트/어댑터 네이밍 & 위치 규칙

```java
// src/test/java/com/example/todo/architecture/NamingRulesTest.java
package com.example.todo.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.example.todo")
public class NamingRulesTest {

    @ArchTest
    static final ArchRule inbound_ports_should_be_named_UseCase =
        classes().that().resideInAnyPackage(PackageRules.APPLICATION_PORT_IN)
                 .should().haveSimpleNameEndingWith("UseCase");

    @ArchTest
    static final ArchRule outbound_ports_should_be_named_Port =
        classes().that().resideInAnyPackage(PackageRules.APPLICATION_PORT_OUT)
                 .should().haveSimpleNameEndingWith("Port");

    @ArchTest
    static final ArchRule adapters_should_be_named_Adapter =
        classes().that().resideInAnyPackage(
                    PackageRules.ADAPTER_IN_WEB,
                    PackageRules.ADAPTER_OUT_PERSISTENCE,
                    PackageRules.ADAPTER_OUT_AUTH
                 )
                 .should().haveSimpleNameEndingWith("Adapter");

    @ArchTest
    static final ArchRule controllers_should_be_in_web_adapter_only =
        noClasses().that().haveSimpleNameEndingWith("Controller")
                   .should().resideOutsideOfPackage(PackageRules.ADAPTER_IN_WEB);
}
```

#### 11.5 금지 패턴 (필드 주입, 순환 의존 등)

```java
// src/test/java/com/example/todo/architecture/ForbiddenPatternsTest.java
package com.example.todo.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.example.todo")
public class ForbiddenPatternsTest {

    @ArchTest
    static final ArchRule no_field_injection =
        noClasses().that().resideInAnyPackage(PackageRules.ROOT)
                   .should().haveAnyFieldsThat().areAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");

    @ArchTest
    static final ArchRule no_cycles_between_main_packages =
        com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
            .matching("com.example.todo.(*)..")
            .should().beFreeOfCycles();
}
```

#### 11.6 도메인 무결성 (엔티티 직접 노출 금지 등)

```java
// src/test/java/com/example/todo/architecture/DomainIntegrityRulesTest.java
package com.example.todo.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(packages = "com.example.todo")
public class DomainIntegrityRulesTest {

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule controllers_should_not_return_domain_entities =
        noClasses().that().resideInAnyPackage(PackageRules.ADAPTER_IN_WEB)
                   .should().dependOnClassesThat().resideInAnyPackage(PackageRules.DOMAIN + "model..");
    // 컨트롤러는 DTO만 반환하고, 도메인 엔티티는 application/service에서 Result DTO로 변환
}
```

> 팁: 프로젝트 루트에서 `./gradlew :api:test` 시 ArchUnit 테스트가 자동 수행되도록 CI에 포함하세요.
