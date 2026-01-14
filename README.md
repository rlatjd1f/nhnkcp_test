# nhnkcp_test

## 지원자 정보
- 이름: 김성리
- 지원 분야: 개발 3팀 backend 개발자

## 프로젝트 개요
- Spring Boot 기반 주문 시스템 과제입니다.
- 상품/주문 도메인과 상태 전이, 재고 처리, 조회 API를 구현했습니다.

## 주요 API

### 상품(Product)
| HTTP Method | URL | 기능 |
| --- | --- | --- |
| POST | `/api/products` | 상품 등록 |
| PUT | `/api/products/{id}` | 상품 수정 |
| GET | `/api/products/{id}` | 상품 단건 조회 |
| GET | `/api/products` | 상품 목록 조회(카테고리/페이징 포함) |

### 주문(Order)
| HTTP Method | URL | 기능 |
| --- | --- | --- |
| POST | `/api/orders` | 주문 생성 |
| PATCH | `/api/orders/{id}/status` | 주문 상태 변경 |
| GET | `/api/orders/{id}` | 주문 단건 조회 |
| GET | `/api/orders` | 주문 목록 조회(상태/기간/페이징 포함) |

## 동시성 및 트랜잭션 전략

**[핵심 전략] 비관적 락(Pessimistic Lock)과 데드락 방지**
재고 차감과 같이 데이터 정합성이 최우선인 로직을 보호하기 위해 DB 레벨의 락을 적용했습니다.

- **구현 내용:**
  - **`PESSIMISTIC_WRITE` 적용:** `select ... for update` 쿼리를 통해 동시 주문 시 재고 정합성을 보장합니다.
  - **데드락(Deadlock) 방지:** 다건 주문 시 교착 상태를 예방하기 위해 상품 ID를 오름차순으로 정렬한 후 순차적으로 락을 획득합니다.
  - **Fail-Fast 전략:** 락 대기 시간을 **3초**(`jakarta.persistence.lock.timeout`)로 제한하여 무한 대기를 방지합니다.
  - **예외 처리:** 락 타임아웃 발생 시 `PessimisticLockingFailureException`을 포착해 `CONCURRENCY_FAILURE`로 일관된 에러 응답을 반환합니다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
@Query("select p from Product p where p.id = :id")
Optional<Product> findByIdForUpdate(@Param("id") Long id);
```

## 성능 최적화 및 확장성 고려

대량의 주문 데이터를 효율적으로 처리하기 위해 다음과 같은 성능 최적화 기법을 적용했습니다.

#### A. N+1 문제 해결 및 페이징 최적화
- **문제:** `@OneToMany` 관계에서 `Fetch Join`과 `Paging`을 함께 사용할 경우, Hibernate가 모든 데이터를 메모리에 로딩한 뒤 페이징 처리하여 **OOM 위험**이 있습니다.
- **해결:** 컬렉션 조회 시 Fetch Join 대신 **`default_batch_fetch_size: 100`** 설정을 적용했습니다.
    ```yaml
    spring:
      jpa:
        properties:
          hibernate:
            default_batch_fetch_size: 100
    ```
- **효과:** `IN` 절을 통해 연관 데이터를 배치 로딩하면서도 메모리 효율적인 페이징 처리가 가능합니다.

#### B. 인덱스 설계
- **전략:** 주문 조회 패턴을 기준으로 인덱스를 구성했습니다.
- **적용:**
  - `idx_orders_status_created_at`: 상태별 조회 및 기간 정렬을 위한 복합 인덱스
  - `idx_orders_created_at`: 기간별 조회 최적화
- **복합 인덱스 컬럼 순서:** `WHERE status = ? ORDER BY created_at` 패턴에 맞춰 정렬해,
  필터링 후 별도의 정렬을 최소화하고 빠르게 결과를 추출합니다.
- **효과:** 대용량 데이터 조회 시 Full Table Scan을 방지하고 조회 속도를 보장합니다.

#### C. 향후 개선 계획
1. **캐싱 전략**
   - **Global Cache:** 상품 상세 정보와 같이 변경이 잦지 않으면서 조회 빈도가 높은 데이터는 Redis와 **Spring Cache(@Cacheable)** 를 연동해 DB 부하를 분산시킵니다.
   - **Local Cache:** 카테고리 정보 등 정적인 데이터는 Caffeine Cache를 적용해 네트워크 비용조차 없는 초고속 조회를 구현합니다.
2. **비동기 이벤트 처리**
   - 주문 완료 후의 '통계 집계'나 '알림 발송' 같은 부가 로직은 Spring Events로 결합도를 낮추고, Kafka(또는 RabbitMQ) 메시지 큐를 도입해 비동기로 처리하여 트랜잭션 응답 시간을 단축합니다.
3. **동시성 제어 고도화**
   - **현재:** 데이터 무결성을 위해 `PESSIMISTIC_WRITE` 사용.
   - **개선:** 충돌률에 따른 이원화 전략 검토.
     - Low Contention: 낙관적 락(`@Version`)과 재시도 전략 적용.
     - High Contention: 비관적 락 유지 또는 Redis 원자 연산으로 처리량 개선.
