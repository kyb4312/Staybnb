# Staybnb

---

에어비엔비를 모티브로 한 숙박 예약 시스템 개발 프로젝트입니다.

프로젝트 구조
```
staybnb/
├── api/
├── batch/
└── database/
```

- `api`  
  - 클라이언트 요청을 처리하는 HTTP API 서버
  - 호스트의 숙소 관리, 게스트의 예약 관리 등의 기능 담당
  - `database` 모듈을 의존하여 데이터 접근

- `batch`  
  - 환율 정보 및 숙박 예약 상태를 갱신하는 배치 작업 수행
  - Spring Batch + Scheduled 기반 스케줄링
  - `database` 모듈을 의존하여 데이터 접근

- `database`
  - 도메인(Entity), JPA Repository, DB 쿼리 등 데이터 계층 담당
  - 다른 모듈(api, batch)의 데이터 접근을 담당하는 모듈

> 각 모듈은 Spring Boot 기반이며, `api`, `batch` 모듈은 실행 가능한 어플리케이션 모듈입니다.
