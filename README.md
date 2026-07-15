# 저기요 (jeogi-yo)

[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL_17-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Spring Security](https://img.shields.io/badge/Spring_Security_JWT-6DB33F?style=flat&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Gemini](https://img.shields.io/badge/Gemini_API-4285F4?style=flat&logo=googlegemini&logoColor=white)](https://ai.google.dev/)

> AI가 상품 설명 작성을 도와주는 **광화문 지역 음식점 배달 서비스**

---

## 목차

- [팀원 소개](#팀원-소개)
- [프로젝트 소개](#프로젝트-소개)
- [서비스 구성 및 실행 방법](#서비스-구성-및-실행-방법)
- [ERD](#erd)
- [시퀀스 다이어그램](#시퀀스-다이어그램)
- [기술 스택](#기술-스택)
- [API 설계](#api-설계)
- [구현 상세](#구현-상세)
- [프로젝트 회고](#프로젝트-회고)
- [소감](#소감)
- [팀 문서](#팀-문서)

---

## 팀원 소개

| 이름       | 담당 파트                                             |
|----------|---------------------------------------------------|
| 차은지 (팀장) | 가게(Store) + 상품(Product) + AI 연동 + 결제(Payment)     |
| 진혜림      | 공통 (Spring Security, JWT, BaseEntity, CommonResponse, 공통 예외 처리) |
| 서주성      | 회원(User) + 배송지(Address)                           |
| 권순혁      | 주문(Order) + 주문상품(OrderItem)                       |
| 서준영      | 카테고리(Category) + 리뷰(Review)                       |

---

## 프로젝트 소개

### 한 줄 요약

**저기요**는 광화문 인근 음식점을 대상으로 한 배달 주문 플랫폼입니다. 고객은 가게·상품을 검색해 주문하고 결제·리뷰까지 남길 수 있으며, 점주는 Google Gemini API를 활용해 상품 설명을 자동으로 생성할 수 있습니다.

### 만들게 된 배경 / 목적

- 배달 서비스의 핵심 흐름(회원가입 → 가게/상품 탐색 → 주문 → 결제 → 리뷰)을 하나의 모놀리식 서비스로 직접 설계하고 구현하며 **도메인 모델링과 REST API 설계 역량**을 키우고자 기획했습니다.
- 점주가 상품을 등록할 때 설명 문구 작성에 드는 시간을 줄이기 위해 **Gemini API 기반 AI 상품 설명 생성 기능**을 도입하고, 모든 요청/응답 이력을 `p_ai_history`에 감사(audit) 로그로 남기도록 설계했습니다.
- 실서비스 운영을 가정해 **Soft Delete, 페이징/정렬 공통 규칙, 역할(Role) 기반 접근 제어** 등 실무에서 요구되는 데이터 정합성·보안 규칙을 API 명세 단계부터 촘촘히 정의했습니다.
- 서비스 범위는 전국 확장을 염두에 두고 설계하되, 1차 개발 단계에서는 **광화문 인근 지역**으로 주문 가능 지역을 한정했습니다.

### 프로젝트 상세

| 항목 | 내용                                                                  |
|---|---------------------------------------------------------------------|
| 프로젝트명 | 저기요 (jeogi-yo)                                                      |
| 형태 | Backend 단일 모놀리식 서비스 (Spring Boot)                                   |
| 대상 지역 | 광화문 인근 (배송지·가게 등록 시 지역 검증)                                          |
| 사용자 역할 | `CUSTOMER`(고객), `OWNER`(점주), `MASTER`(최종 관리자), `MANAGER`(일반 관리자 - 추후 확장 예정) |
| 핵심 도메인 | 회원, 배송지, 카테고리, 가게, 상품, 주문/주문상품, 결제, 리뷰, AI 응답 이력                    |
| AI 연동 | Google AI Studio — Gemini 2.5 Flash-Lite (상품 설명 자동 생성)              |
| 개발 기간 | 2026.07                                                             |

---

## 서비스 구성 및 실행 방법

### 배포 흐름 (CI/CD → AWS EC2)

```
1. git push → 2. Docker build → 3. Docker push(Docker Hub) → 4. Docker pull(EC2) → 5. docker compose up
```

```bash
# 1. 로컬/CI에서 이미지 빌드 및 푸시
docker build -t <dockerhub-id>/jeogi-yo:latest .
docker push <dockerhub-id>/jeogi-yo:latest

# 2. EC2(Ubuntu)에서 이미지 pull 후 기동
docker pull <dockerhub-id>/jeogi-yo:latest
docker compose up -d
```

EC2 위에서 Docker Compose가 Spring Boot 컨테이너와 PostgreSQL 컨테이너를 함께 기동하며, 애플리케이션 로그(Spring Boot Log)와 서버 상태(CPU/메모리/디스크)를 통해 운영 상태를 모니터링합니다.

| 구분 | 프로토콜 | 포트 | 설명 |
|---|:---:|:---:|---|
| 클라이언트 → EC2 | HTTPS | 443 | 웹/모바일 → Spring Boot API |
| EC2 → PostgreSQL | TCP (JDBC) | 5432 | 애플리케이션 → DB |
| EC2 → Gemini API | HTTPS | 443 | AI 상품 설명 요청 |
| SSH 접속 (관리자) | SSH | 22 | EC2 서버 접속 |

### 사전 요구사항

| 항목 | 버전 |
|---|---|
| Java | 17 |
| PostgreSQL | 17.10 |
| Gradle | Wrapper 포함 (`./gradlew`) |
| (배포 시) Docker / Docker Compose | 최신 |

### 로컬 실행 방법

**1. 저장소 클론**

```bash
git clone https://github.com/Georgia-team/jeogi-yo.git
cd jeogi-yo
```

**2. 환경 변수 설정**

프로젝트 경로에 `.env` 파일을 생성합니다. `DB_USERNAME`/`DB_PASSWORD`, `GEMINI_API_KEY`, `SECURITY_JWT_SECRET`을 실제 값으로 채우는 방법은 바로 아래 2-1 ~ 2-3 단계를 참고하세요.

```env
DB_USERNAME=postgres
DB_PASSWORD=your_db_password
SECURITY_JWT_SECRET=위에서-생성한-Base64-문자열
GEMINI_API_KEY=your_gemini_api_key
```

**2-1. PostgreSQL 데이터베이스 준비**

로컬에 설치된 PostgreSQL에 접속해 데이터베이스를 생성합니다.

```sql
CREATE DATABASE delivery_db;
```
* PostgreSQL의 별도의 username, password가 필요합니다.
> [PostgreSQL 연동 가이드](https://app.notion.com/p/team-georgia-pjt-dtl/PostgreSQL-395a26d65cda8040b426ce6e5b57a4d5)를 참고하실 수 있습니다.

**2-2. GEMINI_API_KEY 발급받기**

1. [Google AI Studio API 키 페이지](https://aistudio.google.com/apikey)에 접속해 Google 계정으로 로그인합니다.
2. **API 키 만들기(Create API key)** 버튼을 클릭합니다.
3. 생성된 키를 복사해 위 `.env`의 `GEMINI_API_KEY` 값으로 붙여넣습니다.

> [Gemini API 키 가이드](https://ai.google.dev/gemini-api/docs/api-key?hl=ko)를 참고하실 수 있습니다.

**2-3. SECURITY_JWT_SECRET 생성하기**

`JwtUtil`은 `SECURITY_JWT_SECRET` 값을 Base64로 디코딩해 HS256 서명 키로 사용합니다(`Keys.hmacShaKeyFor`). HS256은 최소 256비트(32바이트) 이상의 키가 필요하므로, 임의의 문자열이 아니라 **충분한 길이의 무작위 바이트를 Base64로 인코딩한 값**을 사용해야 합니다. 아래 중 편한 방법으로 생성하세요.

- **macOS / Linux / Git Bash (Windows)**

  ```bash
  openssl rand -base64 64
  ```

- **Windows PowerShell**

  ```powershell
  [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
  ```

- **Python (OS 무관)**

  ```bash
  python -c "import secrets, base64; print(base64.b64encode(secrets.token_bytes(64)).decode())"
  ```

출력된 Base64 문자열을 그대로 복사해 위 `.env`의 `SECURITY_JWT_SECRET` 값으로 사용합니다. (64바이트 기준 예시일 뿐이며, 32바이트 이상이면 가능합니다.)

**3. 애플리케이션 실행**

```bash
./gradlew bootRun
```

서버 기동 후 Swagger UI: `http://localhost:8080/swagger-ui/index.html`

> `src/main/resources/application.yml`은 기본적으로 `jdbc:postgresql://localhost:5432/delivery_db`를 바라보도록 설정되어 있으며, `spring.jpa.hibernate.ddl-auto=create`로 기동 시 테이블이 자동 생성됩니다.

---

## ERD

```mermaid
erDiagram
    p_user {
        uuid user_id PK
        varchar login_id UK
        varchar nickname UK
        varchar email UK
        varchar phone
        varchar password
        enum role "CUSTOMER/OWNER/MANAGER/MASTER"
        boolean is_deleted
    }
    p_address {
        uuid address_id PK
        uuid user_id FK
        varchar road_address
        varchar detail_address
        varchar zipcode
        boolean is_default
        boolean is_deleted
    }
    p_category {
        uuid category_id PK
        varchar category_name UK
        boolean is_deleted
    }
    p_store {
        uuid store_id PK
        uuid owner_id FK
        uuid category_id FK
        varchar store_name
        varchar address
        enum store_status "OPEN/CLOSED/OUT_OF_BUSINESS"
        boolean is_deleted
    }
    p_product {
        uuid product_id PK
        uuid store_id FK
        uuid category_id FK
        varchar product_name
        text description
        int price
        int stock
        boolean is_hidden
        boolean is_deleted
    }
    p_order {
        uuid order_id PK
        uuid user_id FK
        uuid store_id FK
        uuid address_id FK
        int total_price
        enum order_status
        boolean is_deleted
    }
    p_order_item {
        uuid order_product_id PK
        uuid order_id FK
        uuid product_id FK
        int quantity
        int unit_price
        int item_total_price
    }
    p_payment {
        uuid payment_id PK
        uuid order_id FK
        uuid user_id FK
        enum payment_method "CARD"
        enum payment_status "READY/SUCCESS/CANCEL/FAIL/REFUND"
        int amount
    }
    p_review {
        uuid review_id PK
        uuid user_id FK
        uuid store_id FK
        uuid order_id FK
        int rating "1~5"
        varchar content
    }
    p_ai_history {
        uuid ai_history_id PK
        uuid user_id FK
        uuid product_id FK
        text request_text
        text response_text
        varchar model_name
        enum ai_status "SUCCESS/FAIL"
    }

    p_user ||--o{ p_address : "보유"
    p_user ||--o{ p_store : "운영(OWNER)"
    p_user ||--o{ p_order : "주문"
    p_user ||--o{ p_payment : "결제"
    p_user ||--o{ p_review : "작성"
    p_user ||--o{ p_ai_history : "요청"
    p_category ||--o{ p_store : "업종 분류"
    p_category ||--o{ p_product : "상품 분류"
    p_store ||--o{ p_product : "판매"
    p_store ||--o{ p_order : "주문 접수"
    p_store ||--o{ p_review : "리뷰 대상"
    p_address ||--o{ p_order : "배송지"
    p_order ||--o{ p_order_item : "구성"
    p_product ||--o{ p_order_item : "주문됨"
    p_product ||--o{ p_ai_history : "AI 설명 적용"
    p_order ||--o| p_payment : "결제 1건"
    p_order ||--o| p_review : "리뷰 1건"
```

> 모든 테이블은 `created_at/by`, `updated_at/by`, `is_deleted`, `deleted_at/by`를 공통으로 가지며, 실제 삭제 대신 **Soft Delete**를 사용합니다. 일반 조회는 `is_deleted = false` 조건이 기본 적용됩니다.

### 테이블 목록 (총 10개)

| 도메인 | 테이블 | 설명 |
|---|---|---|
| 회원 | `p_user` | 회원 기본 정보 (CUSTOMER/OWNER/MANAGER/MASTER) |
| 회원 | `p_address` | 배송지 (사용자당 기본 배송지 1개) |
| 상품 분류 | `p_category` | 가게 업종·상품 분류 공통 카테고리 |
| 가게 | `p_store` | 점주가 등록한 가게 |
| 상품 | `p_product` | 가게별 판매 상품 |
| 주문 | `p_order` | 주문 (주문 시점 배송지 스냅샷 포함) |
| 주문 | `p_order_item` | 주문에 포함된 상품 및 수량/금액 |
| 결제 | `p_payment` | 주문 1건당 결제 1건 |
| 리뷰 | `p_review` | 주문 1건당 리뷰 1개 |
| AI | `p_ai_history` | Gemini API 요청/응답 이력 |

---

## 시퀀스 다이어그램

### 1. 회원가입 & 로그인 (JWT 발급/검증)

로그인은 별도 컨트롤러 대신 JwtAuthenticationFilter가 Spring Security 필터 체인에서 처리하고,
이후 요청은 JwtAuthorizationFilter가 JWT를 검증해 인증 정보를 SecurityContext에 등록합니다.

```mermaid
sequenceDiagram
    autonumber
    participant Client as 클라이언트
    participant AuthController as UserAuthController
    participant UserService as UserService
    participant AuthFilter as JwtAuthenticationFilter
    participant AuthManager as AuthenticationManager
    participant UserDetailsSvc as UserDetailsServiceImpl
    participant JwtUtil as JwtUtil
    participant AuthzFilter as JwtAuthorizationFilter

    Note over Client, UserService: 회원가입
    Client ->> AuthController: POST /api/v1/auth/signup
    AuthController ->> UserService: signup(request, Role.CUSTOMER)
    UserService ->> UserService: loginId/nickname/email 중복 검사, BCrypt 암호화
    UserService -->> AuthController: UserSignupResponse
    AuthController -->> Client: 200 회원가입 성공

    Note over Client, JwtUtil: 로그인 (Filter 기반)
    Client ->> AuthFilter: POST /api/v1/auth/login (loginId, password)
    AuthFilter ->> AuthManager: authenticate(UsernamePasswordAuthenticationToken)
    AuthManager ->> UserDetailsSvc: loadUserByUsername(loginId)
    UserDetailsSvc -->> AuthManager: UserDetailsImpl(User)

    alt 인증 성공
        AuthManager -->> AuthFilter: Authentication(principal=UserDetailsImpl)
        AuthFilter ->> JwtUtil: createToken(loginId, role)
        JwtUtil -->> AuthFilter: JWT (Bearer)
        AuthFilter -->> Client: 200 + accessToken (Body + Cookie)
    else 인증 실패
        AuthManager -->> AuthFilter: AuthenticationException
        AuthFilter -->> Client: 401 아이디 또는 비밀번호 불일치
    end

    Note over Client, AuthzFilter: 인증이 필요한 이후 요청
    Client ->> AuthzFilter: 요청 + Authorization: Bearer {JWT}
    AuthzFilter ->> JwtUtil: validateToken() / getUserInfoFromToken()
    AuthzFilter ->> UserDetailsSvc: loadUserByUsername(loginId)
    AuthzFilter ->> AuthzFilter: SecurityContextHolder.setContext(...)
    AuthzFilter -->> Client: 이후 Controller 로직 수행 (또는 토큰 무효 시 401)
```

### 2. 주문 생성 → 결제 → 주문 상태 변경

> 관련 상세 내용: [구현 상세  7. 주문 - 결제 상태 관리](#7-주문---결제-상태-관리)

```mermaid
sequenceDiagram
    autonumber
    actor Customer as CUSTOMER
    actor Owner as OWNER
    participant OrderCtrl as OrderController
    participant OrderSvc as OrderService
    participant PaymentCtrl as PaymentController
    participant PaymentSvc as PaymentServiceImpl

    Customer ->> OrderCtrl: POST /orders (storeId, addressId, items)
    OrderCtrl ->> OrderSvc: createOrder(loginId, request)
    OrderSvc ->> OrderSvc: 가게 OPEN 검증, 배송지 소유자/서비스 지역 검증
    OrderSvc ->> OrderSvc: 상품별 재고 확인 후 차감 (product.decreaseStock)
    OrderSvc ->> OrderSvc: Order + OrderItem 저장 (orderStatus = ORDER_REQUESTED)
    OrderSvc -->> Customer: 201 주문 생성 완료

    Customer ->> PaymentCtrl: POST /orders/{orderId}/payments (CARD)
    PaymentCtrl ->> PaymentSvc: createPayment(orderId, loginId, request)
    PaymentSvc ->> PaymentSvc: 주문 소유자 검증, ORDER_REQUESTED 상태 검증
    PaymentSvc ->> PaymentSvc: existsByOrder_OrderId 로 중복 결제 방지
    PaymentSvc ->> PaymentSvc: Payment 저장 (가상 승인, paymentStatus = SUCCESS)
    PaymentSvc -->> Customer: 200 결제 완료 (주문 상태는 ORDER_REQUESTED 유지)

    Owner ->> OrderCtrl: PATCH /orders/{orderId}/orderstatus (ORDER_ACCEPTED)
    OrderCtrl ->> OrderSvc: updateOrderStatus(loginId, orderId, request)
    OrderSvc ->> OrderSvc: 허용된 상태 전이인지 검증 후 변경
    OrderSvc -->> Owner: 200 상태 변경 완료

    loop COOKING_COMPLETED → DELIVERY_PICKED_UP → DELIVERED
        Owner ->> OrderCtrl: PATCH /orders/{orderId}/orderstatus
        OrderCtrl ->> OrderSvc: updateOrderStatus(...)
        OrderSvc -->> Owner: 200 상태 변경 완료
    end

    Note over Customer, PaymentSvc: 주문 취소 (ORDER_REQUESTED 상태에서 5분 이내에만 가능)
    Customer ->> OrderCtrl: PATCH /orders/{orderId}/cancel
    OrderCtrl ->> OrderSvc: cancelOrder(loginId, orderId, request)
    OrderSvc ->> OrderSvc: 주문 상태/취소 기한 검증, 상품 재고 복구
    OrderSvc ->> PaymentSvc: 결제가 SUCCESS면 payment.cancel(reason)
    OrderSvc ->> OrderSvc: orderStatus = CANCELLED
    OrderSvc -->> Customer: 200 주문 취소 완료
```

### 3. AI 상품 설명 생성 (Gemini 연동)

AI 요청이 실패해도 예외를 그대로 던지지 않고 `p_ai_history`에 `FAIL` 이력을 저장한 뒤 `200 OK` + `aiStatus: FAIL` 형태로 응답합니다. 클라이언트는 HTTP 상태 코드가 아니라 응답 바디의 `aiStatus` 값으로 성공/실패를 판단해야 합니다.

```mermaid
sequenceDiagram
    autonumber
    actor Owner as OWNER
    participant AiCtrl as AiController
    participant AiSvc as AiServiceImpl
    participant GeminiSvc as AiGeminiServiceImpl
    participant Gemini as Gemini API
    participant AiHistoryRepo as AiHistoryRepository

    Owner ->> AiCtrl: POST /products/{productId}/ai-description (requestText)
    AiCtrl ->> AiSvc: createAiDescription(productId, loginId, request)
    AiSvc ->> AiSvc: 상품 조회 + Soft Delete 검증 + 가게 소유자(OWNER) 검증
    AiSvc ->> AiSvc: requestText + "답변을 최대한 간결하게 50자 이하로" 접미사 결합
    AiSvc ->> GeminiSvc: generateDescription(requestText)
    GeminiSvc ->> Gemini: POST /v1beta/models/gemini-2.5-flash-lite:generateContent

    alt Gemini 응답 성공
        Gemini -->> GeminiSvc: candidates[0].content.parts[0].text
        GeminiSvc -->> AiSvc: responseText
        AiSvc ->> AiSvc: product.updateDescription(responseText)
        AiSvc ->> AiHistoryRepo: save(AiHistory.success(...))
        AiSvc -->> Owner: 200 AI 설명 생성 성공 (aiStatus = SUCCESS)
    else Gemini 호출 실패 / 빈 응답
        Gemini -->> GeminiSvc: 오류 또는 빈 candidates
        GeminiSvc -->> AiSvc: Exception
        AiSvc ->> AiHistoryRepo: save(AiHistory.fail(errorMessage))
        AiSvc -->> Owner: 200 응답 (aiStatus = FAIL, errorMessage 포함)
    end
```

---

## 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Spring Boot 3.5 |
| Language | Java 17 |
| Database | PostgreSQL 17.10 |
| ORM | Spring Data JPA / Hibernate, QueryDSL |
| Security | Spring Security + JWT |
| API 문서 | springdoc-openapi (Swagger UI) |
| API 테스트 | Postman |
| AI | Google AI Studio — Gemini API (2.5 Flash-Lite) |
| Container | Docker / Docker Compose |
| Cloud | AWS EC2 (Ubuntu) |
| 형상 관리 | Git / GitHub |

---

## API 설계

### 공통 규칙

- Base URL: `/api/v1`
- 인증: `Authorization: Bearer {JWT}` (로그인 필요 API에 적용)
- 권한(Role): `CUSTOMER`, `OWNER`, `MASTER` (`MANAGER`는 확장 범위로 제외)
- 공통 응답 포맷

  ```json
  {
    "success": true,
    "data": { },
    "message": "string"
  }
  ```

- 페이징: `page` 기본 0, `size`는 10/30/50만 허용(그 외 값은 10), 기본 정렬은 `created_at desc`
- 삭제는 전부 Soft Delete이며, 삭제 시 서버가 `is_deleted`, `deleted_at`, `deleted_by`를 직접 관리 (요청 바디로 받지 않음)
- HTTP 상태 코드: `200`(조회/수정) · `201`(생성) · `400`(검증 실패) · `401`(인증 필요) · `403`(권한 없음) · `404`(리소스 없음) · `409`(중복/규칙 위반) · `500`(서버 오류)

### 도메인별 엔드포인트 개요

| 도메인 | 기본 경로 | 주요 기능 | 권한 |
|---|---|---|---|
| 회원 | `/auth`, `/users` | 회원가입, 로그인(JWT 발급), 내 정보 조회/수정/탈퇴, 회원 검색 | 전체 / MASTER(검색) |
| 배송지 | `/addresses` | 등록·조회·목록·수정·삭제, 기본 배송지 1개 제한 | CUSTOMER |
| 카테고리 | `/categories` | 등록·조회·검색·수정·삭제 (가게/상품 공용 분류) | MASTER(변경) / 전체(조회) |
| 가게 | `/stores` | 등록, 상세/목록 조회, 정보 수정, 영업 상태 변경, 삭제 | OWNER / MASTER |
| 상품 | `/stores/{storeId}/products`, `/products` | 등록(AI 설명 생성 옵션 포함), 조회/검색, 수정, 삭제 | OWNER / MASTER |
| 주문 · 주문상품 | `/orders`, `/stores/{storeId}/orders` | 주문 생성, 상세/목록 조회, 가게별 목록, 상태 변경, 취소 | CUSTOMER / OWNER / MASTER |
| 결제 | `/orders/{orderId}/payments`, `/payments` | 결제 생성(가상 승인), 조회/검색, 취소, 이력 삭제 | CUSTOMER / MASTER |
| 리뷰 | `/orders/{orderId}/reviews`, `/reviews`, `/stores/{storeId}/reviews` | 작성(배송 완료 주문만), 조회/검색, 수정, 삭제 | CUSTOMER / MASTER |
| AI 응답 이력 | `/products/{productId}/ai-description`, `/ai-histories` | Gemini 기반 상품 설명 생성 + 이력 저장/조회/검색 | OWNER(생성) / MASTER(조회) |

주문 상태는 `ORDER_REQUESTED → ORDER_ACCEPTED/ORDER_REJECTED → COOKING_COMPLETED → DELIVERY_PICKED_UP → DELIVERED`(또는 `CANCELLED`) 순으로 전이되며, 각 전이는 요청자의 권한(CUSTOMER/OWNER)에 따라 허용 범위가 다릅니다. 상세 요청/응답 필드와 비즈니스 규칙은 팀 API 명세 문서를 참고하세요.

전체 요청/응답 예시와 필드별 비즈니스 규칙은 서버 기동 후 Swagger UI(`/swagger-ui/index.html`)에서 확인할 수 있습니다.

---

## 구현 상세

### 1. 주요 구현 기능

| 주제 | 기능 / 핵심 내용 | 설명 |
|---|---|---|
| 회원 | 회원가입, 로그인, 내 정보 조회, 회원 수정, 회원 탈퇴, 회원 목록 검색 | JWT 인증 기반으로 로그인 사용자를 식별하고, MASTER는 회원 목록을 조건별로 조회 가능 |
| 관리자 기능 | 회원 목록, 카테고리 관리, AI 이력 관리, 결제 이력 관리 | MASTER 권한으로 운영 관리성 데이터를 조회하고 관리 가능 |
| 배송지 | 배송지 등록, 수정, 삭제, 상세 조회, 목록 조회 | 사용자별 배송지를 관리하고, 주문 생성 시 배송지 정보를 주문상세에 스냅샷으로 저장 |
| 카테고리 | 카테고리 등록, 상세 조회, 검색, 수정, 삭제 | MASTER가 카테고리를 관리하고, 가게/상품 등록 시 카테고리 참조 |
| 가게 | 가게 등록, 상세 조회, 검색, 수정, 상태 변경, 삭제 | OWNER가 본인 가게를 관리하고, CUSTOMER/OWNER/MASTER는 가게 조회 및 검색 가능 |
| 상품 | 상품 등록, 상세 조회, 검색, 수정, 삭제, 숨김 상품 처리 | OWNER는 본인 가게 상품을 관리, CUSTOMER는 숨김 처리되지 않은 상품만 조회 |
| AI | Gemini 기반 상품 설명 생성, AI 이력 상세 조회, AI 이력 검색 | OWNER가 상품 설명을 AI로 생성, MASTER가 AI 응답 이력을 조회/검색 |
| 주문 | 주문 생성, 상세 조회, 주문 목록 검색, 가게별 주문 검색, 상태 변경, 주문 취소 | 주문 생성 시 재고를 차감, 주문 취소 시 재고를 복구 |
| 결제 | 결제 생성, 상세 조회, 목록 검색, 결제 취소, 결제 이력 삭제 | 주문 단위로 결제를 생성하고, 결제 취소 시 주문 취소와 연동해 주문-결제 상태 정합성을 유지 |
| 리뷰 | 리뷰 등록, 상세 조회, 가게별 리뷰 검색, 수정, 삭제 | 배송 완료된 주문을 기준으로 리뷰 작성 가능 여부와 중복 리뷰를 검증 |

### 2. 기술 적용 내용

| 기술 | 적용 내용 | 설명 |
|---|---|---|
| Spring Security | 인증/인가 구조 구성 | JWT 필터와 SecurityConfig를 통해 API 접근 제어 |
| JWT | Access Token 발급 및 검증 | 로그인 성공 시 토큰을 발급하고, 요청마다 토큰을 검증 |
| BCrypt | 비밀번호 암호화 | 회원가입 시 비밀번호를 암호화하여 저장 |
| `@PreAuthorize` | 역할 기반 접근 제어 | Controller 진입 단계에서 CUSTOMER/OWNER/MASTER 권한 검증 |
| `@AuthenticationPrincipal` | 로그인 사용자 추출 | 요청 파라미터가 아닌 인증 객체에서 로그인 사용자의 `loginId` 추출 |
| JPA / Hibernate | Entity 매핑, 연관관계, UUID PK | ERD 기준으로 Entity를 구성하고 UUID 식별자 사용 |
| QueryDSL | 동적 검색 조건 처리 | 검색 조건이 다양한 목록 API에서 조건 조합, 페이징, 정렬 처리 |
| JPA Auditing | 생성/수정 시간 자동 기록 | `createdAt`, `updatedAt` 같은 공통 이력 자동 관리 |
| CommonResponse | API 응답 포맷 통일 | 성공 여부, 메시지, 데이터를 동일 구조로 응답 |
| PageResponse / PageUtil | 페이지 응답 및 요청 보정 | 목록 검색 응답 구조와 page/size/sort 처리 통일 |
| Swagger | API 문서 및 인증 테스트 | Bearer Token 인증을 적용해 Swagger에서 API 테스트 가능 |
| JUnit / Mockito | 단위 테스트 | 권한, 상태 변경, 실패 케이스, 페이지 처리 검증 |

### 3. 보안 및 권한 설계

| 항목 | 적용 내용 | 설명 |
|---|---|---|
| 인증 기준 | JWT 기반 인증 | 세션이 아닌 Access Token으로 로그인 상태 확인 |
| 사용자 식별 | loginId를 인증 객체에서 추출 | 클라이언트가 넘긴 사용자 값이 아니라 서버가 검증한 인증 정보 사용 |
| 권한 분리 | CUSTOMER / OWNER / MASTER | 사용자 역할에 따라 API 접근 범위 분리 |
| CUSTOMER 권한 | 본인 데이터 중심 접근 | 본인 주문, 결제, 배송지, 리뷰 관리 |
| OWNER 권한 | 본인 가게 데이터 관리 | 본인 가게, 상품, 가게 주문 관리 |
| MASTER 권한 | 전체 관리 권한 | 회원 목록, AI 이력, 결제 이력 등 운영 데이터 관리 |
| 탈퇴 회원 차단 | isDeleted=false 사용자만 인증 | 탈퇴 회원은 로그인 및 JWT 인증 대상에서 제외 |
| 소유자 검증 | Service 계층에서 추가 검증 | Controller 권한 검증 이후에도 본인 가게/상품인지 재검증 |

### 4. 데이터 관리 전략

| 항목 | 적용 내용 | 설명 |
|---|---|---|
| UUID PK | 각 Entity의 PK를 UUID로 통일 | ERD의 `user_id`, `store_id`, `product_id` 등과 1:1 대응 |
| BaseEntity | 공통 이력 필드 관리 | 생성/수정/삭제 시간과 사용자 정보를 공통으로 관리 |
| Soft Delete | 실제 삭제 대신 삭제 상태 저장 | `is_deleted`, `deleted_at`, `deleted_by`로 삭제 이력 보존 |
| 조회 조건 | 삭제 데이터 제외 | 일반 조회/수정/삭제 대상은 `isDeleted=false` 조건으로 조회 |
| 감사 필드 응답 제외 | 응답 DTO 분리 | Postman/Swagger 응답에는 불필요한 내부 감사 필드 노출 최소화 |
| 배송지 스냅샷 | 주문 생성 시 주소 정보 복사 | 이후 배송지가 수정되어도 주문 당시 주소 정보를 유지 |
| 재고 관리 | 주문 생성/취소 시 재고 차감/복구 | 주문 상태와 상품 재고의 정합성 유지 |

### 5. QueryDSL 적용 내용

| 도메인 | 적용 내용 | 설명 |
|---|---|---|
| Store | 카테고리, 키워드, 페이징, 정렬 검색 | 가게 목록을 조건별로 동적 조회 |
| Product | 가게, 카테고리, 키워드, 숨김 여부 검색 | 권한에 따라 숨김 상품 노출 여부를 다르게 처리 |
| AI History | AI 상태, 상품, 사용자 기준 검색 | MASTER가 AI 응답 이력을 조건별로 조회 |
| Payment | 결제 상태, 사용자, 삭제 포함 여부 검색 | 결제 이력을 운영/사용자 관점에서 조회 |
| Order | 역할별 주문 검색, 가게별 주문 검색 | CUSTOMER/OWNER/MASTER 역할에 따라 조회 범위 분리 |
| User | 회원 역할, 키워드 기준 검색 | MASTER가 회원 목록을 조건별로 조회 |
| Review | 가게, 평점, 페이징, 정렬 검색 | 특정 가게의 리뷰를 평점 조건과 함께 동적 조회 |

### 6. AI 기능 설명

| 항목 | 적용 내용 | 설명 |
|---|---|---|
| AI 모델 | Gemini API 사용 | 상품 설명 생성을 위한 외부 AI API 연동 |
| 사용 권한 | OWNER만 생성 가능 | 본인 가게 상품에 대해서만 AI 설명 생성 가능 |
| 상품 설명 반영 | AI 응답을 상품 설명으로 저장 | AI 결과를 실제 상품 설명 필드에 반영 |
| 성공 이력 | SUCCESS 상태 저장 | 요청 프롬프트, 응답 내용, 모델명, 상품/사용자 정보 저장 |
| 실패 이력 | FAIL 상태 저장 | 외부 API 실패 시에도 실패 이력을 남겨 추적 가능 |
| 이력 관리 | MASTER만 조회/검색 가능 | AI 사용 이력을 운영 관리 데이터로 분리 |

### 7. 주문 - 결제 상태 관리

| 상황 | 처리 내용 | 설명 |
|---|---|---|
| 주문 생성 | 상품 재고 확인 후 재고 차감 | 주문 생성과 재고 차감을 하나의 흐름으로 처리 |
| 결제 성공 | `PaymentStatus.SUCCESS` 저장 | 결제 성공은 주문 수락과 구분 |
| 주문 상태 | 결제 성공 후에도 `ORDER_REQUESTED` 유지 | OWNER가 별도로 주문 수락 여부를 결정 |
| 주문 취소 | `OrderStatus.CANCELLED` 처리 | 주문 요청 상태 등 취소 가능한 조건을 검증한 뒤 주문을 취소 상태로 변경 |
| 결제 취소 | `PaymentStatus.CANCEL` 처리 | 결제 성공 상태인 건만 취소 가능하며, 취소 시 주문 취소 흐름과 함께 처리 |
| 취소 연동 | 주문 취소 ↔ 결제 취소 연동 | 주문과 결제 중 한쪽만 취소되는 상황을 방지해 상태 정합성 유지 |
| 재고 복구 | 주문/결제 취소 시 상품 재고 복구 | 취소된 주문의 주문상품 수량만큼 재고를 다시 복구 |
| 재결제 정책 | 같은 주문으로 중복 결제 불가 | 주문 1건당 결제 1건 정책 유지 |

### 8. 트러블슈팅

#### 1. 로그인 처리 위치 (Controller vs Filter) 충돌

- **문제**: `UserAuthController`에 `/auth/login` 엔드포인트를 만들어두었는데, 별도로 `JwtAuthenticationFilter`(`UsernamePasswordAuthenticationFilter` 상속)도 같은 경로(`setFilterProcessesUrl("/api/v1/auth/login")`)를 가로채도록 구현되어 있어 로그인 로직이 두 곳에 중복될 뻔했습니다.
- **원인**: Spring Security의 `UsernamePasswordAuthenticationFilter` 기반 로그인은 필터 체인 단계에서 요청을 가로채 처리하기 때문에, 같은 URL에 컨트롤러 메서드를 둬도 필터가 먼저 요청을 소비해 컨트롤러까지 도달하지 않습니다. 두 구현이 공존하면 "왜 컨트롤러 로그가 안 찍히지?"와 같은 혼란이 생깁니다.
- **해결**: 로그인 책임을 `JwtAuthenticationFilter` 한 곳으로 확정하고, `UserAuthController`의 `login()` 메서드는 주석 처리해 회원가입(`signup`, `signup/owner`) API만 남겼습니다. 인증/인가처럼 필터 체인이 관여하는 기능은 컨트롤러가 아니라 필터 레벨에서 처리한다는 원칙을 팀 컨벤션으로 정리했습니다.

#### 2. 결제 중복 생성 방지 (애플리케이션 검증 + DB 제약의 이중 방어)

- **문제**: 네트워크 재시도나 더블 클릭으로 같은 주문에 결제 요청이 두 번 들어올 경우, 애플리케이션 로직만 믿으면 동시 요청 상황에서 결제가 중복 생성될 수 있습니다.
- **원인**: `paymentRepository.existsByOrder_OrderId(orderId)` 같은 애플리케이션 레벨 검증은 요청 사이에 경합 조건(race condition)이 끼어들 여지가 있어 완벽한 방어가 되지 않습니다.
- **해결**: `PaymentServiceImpl`에서 애플리케이션 레벨로 먼저 중복을 검사하되, DB 스키마의 `p_payment.order_id`에 `UNIQUE` 제약을 함께 걸어 "주문 1건당 결제 1건"을 이중으로 보장했습니다. 애플리케이션 검증은 사용자에게 친절한 409 응답을, DB 제약은 최후의 데이터 정합성 보루 역할을 합니다.

#### 3. 주문 취소 ↔ 결제 취소 상태 동기화

- **문제**: 주문만 취소되고 결제는 `SUCCESS`로 남거나, 반대로 결제만 취소되고 주문 상태는 그대로인 상태 불일치가 발생할 수 있었습니다.
- **원인**: 주문(`Order`)과 결제(`Payment`)가 서로 다른 애그리거트로 분리되어 있어, 한쪽 상태 변경 로직만 구현하면 다른 쪽이 갱신되지 않습니다.
- **해결**: `OrderService.cancelOrder()`에서 연결된 결제가 `SUCCESS` 상태면 `payment.cancel()`을 함께 호출하고, 반대로 `PaymentServiceImpl.cancelPayment()`에서도 `orderService.cancelByPayment()`를 호출해 주문 상태를 `CANCELLED`로 맞춥니다. 양방향 진입점 모두에서 상대 도메인의 상태를 갱신하도록 맞춰 어느 쪽에서 취소를 시작해도 정합성이 유지되도록 했습니다.

#### 4. Soft Delete 데이터가 일반 조회에 노출되는 문제

- **문제**: 초기에는 `findById()`만 사용하는 조회 메서드가 있어, 삭제(`is_deleted = true`) 처리된 회원·가게·상품이 일반 조회 API 응답에 그대로 나타났습니다.
- **원인**: JPA의 기본 `findById()`는 `is_deleted` 컬럼을 알지 못하므로 Soft Delete 여부와 무관하게 데이터를 반환합니다.
- **해결**: 모든 도메인의 일반 조회·검색·수정·삭제 로직에서 `findByXxxIdAndIsDeletedFalse()` 형태의 파생 쿼리 메서드로 통일하고, QueryDSL 검색 조건에도 `condition.and(entity.isDeleted.isFalse())`를 기본으로 포함시켜 삭제된 데이터가 섞여 나오지 않도록 했습니다.

#### 5. 예외 처리 방식 혼재 (`ResponseStatusException` vs `BusinessException`)

- **문제**: `OrderService`처럼 초기에 작성된 코드는 `ResponseStatusException(HttpStatus.XXX, "메시지")`를 그때그때 던지는 반면, `PaymentServiceImpl`/`AiServiceImpl`은 `BusinessException(GlobalErrorCode.XXX)`를 던지고 `GlobalExceptionHandler`가 이를 `CommonResponse` 형식으로 변환합니다.
- **원인**: 여러 명이 다른 시점에 도메인을 구현하면서 예외 처리 컨벤션이 먼저 정착하지 못한 상태로 개발이 진행되어, 메시지 문구와 응답 포맷(에러 코드 유무 등)이 도메인마다 조금씩 달라졌습니다.
- **해결**: 신규/리팩터링 대상 도메인부터 `BusinessException + ErrorCode(GlobalErrorCode) + GlobalExceptionHandler` 조합으로 옮기는 것을 팀 공통 방향으로 정하고, `ResponseStatusException`을 쓰는 기존 코드는 우선순위를 정해 순차적으로 `BusinessException` 기반으로 전환하고 있습니다.

#### 6. AI 응답 실패가 HTTP 에러로 드러나지 않는 문제

- **문제**: Gemini API 호출이 실패해도 `AiServiceImpl.createAiDescription()`이 예외를 밖으로 던지지 않고 `AiHistory.fail(...)`을 저장한 뒤 `200 OK`로 응답해, 클라이언트가 실패를 놓치기 쉬웠습니다.
- **원인**: 상품 등록/설명 생성 흐름에서 AI 실패로 전체 트랜잭션이 롤백되면 사용자 경험이 나빠지기 때문에, AI 실패는 "요청 자체는 처리되었지만 AI 결과만 실패"로 다루기로 설계했습니다.
- **해결**: 응답 바디의 `aiStatus` 필드(`SUCCESS`/`FAIL`)로 성공 여부를 판단하도록 API 문서(Swagger)에 명시하고, 실패 시 `errorMessage`를 함께 내려주도록 했습니다. 프런트/클라이언트 개발 가이드에도 "AI 응답은 HTTP 상태 코드가 아니라 `aiStatus`로 분기할 것"을 명시했습니다.

#### 7. QueryDSL Q클래스 인식 실패

- **문제**: 도메인 Entity를 추가한 직후 `QOrder`, `QStore` 같은 Q클래스를 찾지 못해 컴파일 에러가 나는 경우가 있었습니다.
- **원인**: `build.gradle`에서 QueryDSL Q클래스 생성 경로를 `build/generated/querydsl`로 커스터마이징했는데, IDE가 해당 디렉터리를 Generated Sources Root로 다시 인식하지 못하거나 `./gradlew clean` 이후 재생성을 누락하면 Q클래스가 비어 있는 상태로 남습니다.
- **해결**: Q클래스 인식 문제가 발생하면 `./gradlew clean compileJava`로 재생성 후 IDE에서 Gradle 프로젝트를 다시 로드하도록 팀에 안내했습니다. `sourceSets.main.java.srcDirs`에 생성 경로를 명시적으로 추가해 둔 것도 이 문제를 줄이기 위한 설정입니다.

#### 8. [@Transactional 적용범위](https://app.notion.com/p/team-georgia-pjt-dtl/Transactional-39ea26d65cda80639ee4e373b002b865)

### 9. 협업 과정에서 정리한 정책

| 정책 | 정리 내용 | 설명 |
|---|---|---|
| `userId` / `loginId` 분리 | `userId`는 UUID, `loginId`는 String | DB PK/FK와 로그인 식별자를 명확히 분리 |
| 감사 필드 기준 | `createdBy`, `updatedBy`, `deletedBy`는 loginId 저장 | 사람이 식별 가능한 로그인 ID 기준으로 이력 기록 |
| 일반 회원가입 | CUSTOMER 기본 가입 | 일반 사용자는 CUSTOMER 권한으로 가입 |
| OWNER 회원가입 | 별도 OWNER 가입 API | 가게 생성과 회원 생성을 분리해 도메인 결합도 완화 |
| MASTER 생성 | 요청 body로 받지 않음 | seed 또는 관리 방식으로 MASTER 계정 생성 |
| OWNER 탈퇴 | 활성 가게가 있으면 탈퇴 불가 | 탈퇴 후 가게 관리 주체가 사라지는 문제 방지 |
| 활성 가게 기준 | `isDeleted=false` + `OPEN/CLOSED` | OUT_OF_BUSINESS는 탈퇴 가능 상태로 판단 |
| 카테고리 삭제 | 연결된 Store/Product가 있으면 삭제 불가 | 참조 데이터 보호 |
| 결제 정책 | 주문 1건당 결제 1건 | 결제 이력과 주문 관계를 단순하고 명확하게 유지 |
| 재결제 정책 | 취소 후 같은 주문 재결제 불가 | 재결제가 필요하면 새 주문 생성 |
| 삭제 데이터 정책 | 삭제된 데이터는 등록/수정/조회 대상에서 제외 | Soft Delete 데이터와 활성 데이터를 분리 |
| 공통 응답 정책 | `CommonResponse<T>` 사용 | 응답 구조 일관성 확보 |
| 페이지 정책 | `PageResponse<T>` + `PageUtil` 사용 | 검색 API 페이지 응답 통일 |
| 예외 정책 | `BusinessException + ErrorCode + GlobalExceptionHandler` 방향 | 도메인별 예외 응답을 공통 형식으로 맞추는 기준 정리 |
| 권한 표현식 정책 | `@PreAuthorize`는 `hasRole(...)` 형태로 통일 | `hasAuthority('ROLE_...')`와 혼용하면 `ROLE_` 접두사 누락 실수가 잦아, 신규/리팩터링 대상 API부터 순차 통일 |
| 로그인 사용자 타입 정책 | `@AuthenticationPrincipal`은 `UserDetails` 타입으로 통일 | 구현체(`UserDetailsImpl`)를 직접 받는 일부 기존 코드도 순차적으로 통일 |

---

## 프로젝트 회고

### 잘된 점과 이유

| 구분 | 잘된 점 | 이유 |
|---|---|---|
| 기술 구현 | JWT 기반 인증과 `@PreAuthorize` 권한 제어 적용 | 로그인 사용자를 요청 파라미터가 아니라 인증 객체에서 가져오도록 바꾸면서 보안성이 좋아졌고, 역할별 API 접근 제어가 명확해졌다. |
| 기술 구현 | Soft Delete + Auditing 구조 적용 | 실제 데이터를 삭제하지 않고 `is_deleted`, `deleted_at`, `deleted_by`로 관리해 주문, 결제, 리뷰 같은 운영 이력을 보존할 수 있었다. |
| 기술 구현 | QueryDSL 기반 검색 구현 | 가게, 상품, 주문, 결제, 리뷰처럼 검색 조건이 여러 개인 API에서 조건을 동적으로 조합할 수 있어 검색 기능 확장성이 좋아졌다. |
| 기술 구현 | 주문-결제 상태 연동 처리 | 주문 취소와 결제 취소가 따로 움직이면 상태 불일치가 생길 수 있어, 주문 상태와 결제 상태를 함께 맞추도록 설계했다. |
| 기술 구현 | 공통 응답과 페이지 응답 구조 통일 | `CommonResponse`, `PageResponse`, `PageUtil`을 적용해 API 응답 형태가 도메인마다 달라지는 문제를 줄였다. |
| 협업 방식 | 도메인별 책임을 나누고 결합 지점을 따로 점검 | User, Store, Product, Order, Payment처럼 담당 도메인은 분리하되, 회원 탈퇴, 주문-결제, 카테고리 삭제처럼 연결되는 부분은 별도 정책으로 정리했다. |
| 협업 방식 | 정책 결정 사항을 문서화하며 진행 | OWNER 탈퇴 기준, 재결제 허용 여부, Soft Delete 조회 기준 등 애매한 부분을 문서로 정리해 팀원 간 이해 차이를 줄였다. |
| 협업 방식 | 브랜치 병합 후 컴파일/테스트로 검증 | 각자 구현한 기능을 develop에 병합하면서 `compileJava`, 테스트를 통해 충돌이나 영향 범위를 빠르게 확인했다. |

### 협업 간 발생한 문제와 해결 방안

| 문제 | 원인 | 해결 방안 |
|---|---|---|
| 로그인 처리 위치가 Controller와 Filter로 나뉠 수 있었음 | Spring Security 로그인 필터가 `/api/v1/auth/login` 요청을 먼저 처리하는 구조를 팀원들이 처음에 명확히 공유하지 못함 | 로그인 책임을 `JwtAuthenticationFilter`로 통일하고, Controller는 회원가입 관련 API만 담당하도록 정리 |
| `loginId`를 요청 파라미터로 받는 방식의 보안 문제 | 초기 개발 단계에서는 테스트 편의를 위해 `loginId`를 직접 넘겼지만, 실제 서비스에서는 다른 사용자처럼 요청할 위험이 있음 | `@AuthenticationPrincipal`로 로그인 사용자를 추출하도록 변경 |
| 도메인마다 응답 형식이 달랐음 | 각 담당자가 기능 구현을 먼저 진행하면서 응답 DTO와 페이지 응답 구조가 제각각 생김 | `CommonResponse<T>`, `PageResponse<T>`를 적용해 응답 구조를 통일 |
| 예외 처리 방식이 섞임 | `IllegalArgumentException`, `ResponseStatusException`, `BusinessException`이 도메인별로 혼재 | 공통 방향을 `BusinessException + GlobalErrorCode + GlobalExceptionHandler`로 정하고 순차적으로 전환 |
| Soft Delete 데이터가 조회될 가능성 | JPA 기본 `findById()`는 `is_deleted` 여부를 고려하지 않음 | 일반 조회/수정/삭제 대상은 `findBy...AndIsDeletedFalse` 기준으로 변경 |
| 주문과 결제 상태가 어긋날 수 있음 | 주문 취소와 결제 취소가 서로 다른 도메인에서 처리됨 | 주문 취소 시 결제 취소, 결제 취소 시 주문 취소를 연동해 상태 정합성을 맞춤 |
| 브랜치 병합 후 테스트 실패 발생 | 여러 브랜치에서 Entity 관계, 예외 처리, 테스트 fixture가 동시에 변경됨 | 병합 순서를 정하고, 컴파일 후 실패 테스트를 담당자별로 나눠 수정 |
| 도메인 간 정책 기준이 애매했음 | OWNER 탈퇴, 카테고리 삭제, 재결제 허용 여부처럼 한 도메인만 보고 결정하기 어려운 규칙이 있었음 | 정책 후보를 정리한 뒤 팀 논의로 기준을 확정하고 각 도메인에 반영 |

---

## 소감

| 이름 | 소감 |
|---|---|
| 진혜림 | 프로젝트에서 공통 기능을 구현하며 Security와 JWT의 인증 흐름을 이해하고, Exception 처리를 통해 다양한 예외 상황과 클라이언트와의 협업 중요성을 배울 수 있었습니다. 또한 기존에는 GitHub Desktop을 주로 사용했지만, 이번 프로젝트에서는 Git 명령어를 중심으로 협업을 진행하면서 브랜치 관리, 병합, 충돌 해결 등 Git의 기본적인 사용법을 익힐 수 있었고, 협업을 통해 코드 구현 능력뿐만 아니라 개발 흐름에 대한 이해와 의사소통 능력까지 함께 성장할 수 있는 의미 있는 경험이었습니다. |
| 서주성 | Git Merge를 담당하면서 Git 사용법에 더 익숙해졌고 프로젝트 개발을 진행하면서 문서화가 중요하다는 것을 느꼈습니다. 특히 API 명세서를 잘 작성해놓으니까 개발하는 속도가 빨라지고, API 명세서에 맞추어 테스트 코드도 빠르게 작성할 수 있었습니다. |
| 차은지 | 이번 프로젝트를 진행하면서 단순히 API를 구현하는 것보다 도메인 간 정책을 맞추는 일이 더 중요하다는 것을 느꼈습니다. 인증, 권한, Soft Delete, 주문-결제 상태 연동처럼 여러 기능이 연결될수록 초반 설계와 팀 내 기준 정리가 중요했습니다. Git 병합과 테스트 과정에서 충돌도 있었지만, 함께 정책을 정리하고 개선하면서 협업 경험을 많이 쌓을 수 있었습니다. |
| 권순혁 | 프로젝트를 통해 요구사항 분석, ERD 설계, API 명세서 작성부터 엔티티·리포지터리·서비스 구현이 되기까지 코드를 작성하여 기능이 완성되기까지의 경험을 통해, 코드를 작성하는 것 못지않게 그 앞뒤로 필요한 설계와 협의, 검증 절차가 이렇게 많다는 것을 느끼게 되었고, 이를 통해 프로젝트를 체계적으로 바라보는 시야를 가지게 되었습니다. |
| 장준영 | 중간에 합류해서 프로젝트의 follower로 임했다. Git을 본격적으로 써본 것도, 이렇게 회의를 많이 한 것도 처음이다. 훌륭한 팀원들이 많아서 보고 배울 점이 많았다. 제공되는 Annotation과 Spring boot 내장 함수들을 이용해보면서 실무에서는 코드를 재활용하는 경우가 많다는 것도 알았다. 다시 하면 더 잘할 수 있을 것 같다. |

---

## 팀 문서

| 문서      | 비고                                                                                                               |
|---------|------------------------------------------------------------------------------------------------------------------|
| 팀 노션    | [Project / 음식 주문 관리 시스템](https://app.notion.com/p/team-georgia-pjt-dtl/Project-f75a26d65cda8273b49781ec80167a20) |
| 팀 회고    | [프로젝트 회고 (Q&A 정리)](https://app.notion.com/p/team-georgia-pjt-dtl/Q-A-391a26d65cda80ec8b36ddc14711c1f2)       |
| Swagger | `http://localhost:8080/swagger-ui/index.html`                                                                    |

---

<p align="center">저기요 (jeogi-yo) — Built with Spring Boot</p>
