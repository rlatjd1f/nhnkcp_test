# nhnkcp_test

## 지원자 정보
- 성함: 김성리

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

### 동시성 및 트랜잭션
- 상품 재고 갱신 시 `PESSIMISTIC_WRITE` 락을 사용해 동시 주문에서 재고 정합성을 유지합니다.
- 주문 생성과 상태 변경은 트랜잭션 경계 내에서 처리해 중간 실패 시 롤백되도록 구성했습니다.
- 재고 조회/갱신 시점에 비관적 락을 적용한 코드 예시는 아래와 같습니다.
- 락 대기 시간을 3초로 제한해 무한 대기를 방지했습니다.
- 다건 주문 시 데드락을 피하기 위해 상품 ID를 오름차순으로 정렬한 뒤 락을 획득합니다.
- 락 타임아웃으로 실패하는 경우 `CONCURRENCY_FAILURE`로 일관된 에러 응답을 반환합니다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
@Query("select p from Product p where p.id = :id")
Optional<Product> findByIdForUpdate(@Param("id") Long id);
```

### 성능 고려 및 개선안
- 주문 목록/상태/기간 조회는 페이징을 기본으로 사용해 대량 데이터에서 메모리 사용을 제어합니다.
- 주문 데이터가 증가하면 인덱스(주문 상태, 생성일) 추가와 읽기 전용 쿼리 최적화가 필요합니다.
- 자주 조회되는 목록은 캐시 적용(예: 상태별 조회)과 비동기 집계로 응답 시간을 개선할 수 있습니다.
