sb-ecom (Spring Boot E‑commerce API) — WIP

Overview

A work‑in‑progress Spring Boot REST API for an e‑commerce system. Currently includes Category endpoints with validation and centralized error handling. Uses an in‑memory H2 database for development.

Tech stack

- Java 17, Spring Boot 3.5 (Web, Data JPA, Validation)
- H2 (in‑memory), Maven

Quick start

Prerequisites: JDK 17+, Maven 3.9+

Run locally:

```bash
mvn spring-boot:run
```

- App: http://localhost:8080
- H2 console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:test` (username: `sa`, empty password)

API endpoints

- GET `/api/public/categories` — list categories
- POST `/api/public/categories` — create category
  - body:
    ```json
    { "categoryName": "Shoes" }
    ```
- PUT `/api/public/categories/{categoryId}` — update category name
  - body:
    ```json
    { "categoryName": "Updated Name" }
    ```
- DELETE `/api/admin/categories/{categoryId}` — delete category

Examples

```bash
curl -s http://localhost:8080/api/public/categories

curl -s -X POST http://localhost:8080/api/public/categories \
  -H "Content-Type: application/json" \
  -d '{"categoryName":"Shoes"}'

curl -s -X PUT http://localhost:8080/api/public/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"categoryName":"Boots"}'

curl -s -X DELETE http://localhost:8080/api/admin/categories/1
```

Error handling

Custom exceptions (`APIException`, `ResourceNotFoundException`) are mapped by a global handler to produce meaningful messages with appropriate HTTP status codes.

Project structure (selected)

```
src/main/java/com/ecommerce/project/
  SbEcomApplication.java
  category/CategoryController.java
  model/Category.java
  service/{CategoryService, CategoryServiceImpl}.java
  repositories/CategoryRepository.java
  exceptions/{APIException, ResourceNotFoundException, MyGlobalExceptionHandler}.java
src/main/resources/application.properties
```

Notes

- This repo is WIP; the `Category` entity mapping and DB persistence details may change (e.g., adding `@Entity`, `@Id`, `@GeneratedValue`).
- Validation via `spring-boot-starter-validation` is enabled for request bodies.

Build

```bash
mvn clean package
```

License

TBD


