# Spring Boot E-commerce API

> **Status:** Functional learning project — suitable for local development, not production hardened.

A RESTful e-commerce backend built with Spring Boot. It supports product catalog management, user registration and sign-in, JWT authentication, shopping carts, addresses, and order placement backed by PostgreSQL.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Implemented Features](#implemented-features)
- [Domain Models](#domain-models)
- [API Endpoints](#api-endpoints)
- [Security Notes](#security-notes)
- [Project Structure](#project-structure)
- [Run Locally](#run-locally)
- [Configuration](#configuration)
- [Current Limitations](#current-limitations)

---

## Overview

The application follows a conventional Spring layered architecture: controllers expose REST endpoints, services hold business logic, repositories persist JPA entities, and DTOs separate the API contract from database models. It includes pagination, sorting, keyword search, product image updates, and token-based authentication.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.6**
  - Spring Web (REST API)
  - Spring Data JPA (Database persistence)
  - Spring Security (Authentication & Authorization)
  - Spring Validation (Request validation)
- **PostgreSQL** (Production database)
- **JWT** (JSON Web Tokens via `jjwt` 0.12.5)
- **Lombok** (Boilerplate reduction)
- **ModelMapper** (Entity-DTO mapping)
- **Maven** (Dependency management)

---

## Implemented Features

- **Category Management**
  - List categories with pagination and sorting
  - Create, update, and delete categories
  - Validation on category names

- **Product Management**
  - Create products linked to categories
  - List all products with pagination and sorting
  - Search products by category
  - Search products by keyword
  - Update and delete products
  - Upload product images

- **Authentication and Users**
  - Sign up and sign in endpoints
  - BCrypt password hashing
  - JWT generation and request filtering
  - User, role, and address entities

- **Shopping and Orders**
  - Add, update, view, and remove cart items
  - Create and manage delivery addresses
  - Place orders with payment details
  - View a user's orders and update order status

- **Error Handling**
  - Global exception handler
  - Custom exceptions (`APIException`, `ResourceNotFoundException`)
  - Validation error responses

- **Cross-cutting Concerns**
  - Request validation and DTO mapping
  - Global exception handling
  - Pagination and sorting constants

---

## Domain Models

### Core Entities

- **Category**
  - `categoryId` (Primary Key)
  - `categoryName` (Required, min 2 characters)
  - `description`
  - One-to-Many relationship with `Product`

- **Product**
  - `productId` (Primary Key)
  - `productName` (Required, min 2 characters)
  - `productDescription` (Required, min 6 characters)
  - `productImage` (Image path)
  - `productPrice` (Positive or zero)
  - `specialPrice`, `discount`
  - `productQuantity`
  - Many-to-One relationship with `Category`
  - Many-to-One relationship with `User` (seller)

- **User**
  - `userId` (Primary Key)
  - `username` (Unique, 3-50 characters)
  - `email` (Unique, valid email format)
  - `password` (4-120 characters)
  - Many-to-Many relationship with `Role`
  - One-to-Many relationship with `Product` (seller)
  - Many-to-Many relationship with `Address`

- **Role**
  - `roleId` (Primary Key)
  - `roleName` (Enum: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SELLER`)

- **Address**
  - `addressId` (Primary Key)
  - `street`, `building`, `city`, `state`, `country`, `pincode`
  - Many-to-Many relationship with `User`

---

## API Endpoints

### Categories

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/public/categories` | List all categories (paginated) | Public |
| `POST` | `/api/public/categories` | Create a new category | Public |
| `PUT` | `/api/public/categories/{categoryId}` | Update category | Public |
| `DELETE` | `/api/admin/categories/{categoryId}` | Delete category | Admin |

**Query Parameters (for GET):**
- `pageNumber` (default: 0)
- `pageSize` (default: 50)
- `sortBy` (default: "categoryId")
- `sortOrder` (default: "asc")

### Products

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| `GET` | `/api/public/products` | List all products (paginated) | Public |
| `GET` | `/api/public/categories/{categoryId}/products` | Get products by category | Public |
| `GET` | `/api/public/products/keyword/{keyword}` | Search products by keyword | Public |
| `POST` | `/api/admin/categories/{categoryId}/product` | Create a product | Admin |
| `PUT` | `/api/admin/products/{productId}` | Update product | Admin |
| `DELETE` | `/api/admin/products/{productId}` | Delete product | Admin |
| `PUT` | `/api/products/{productId}/image` | Update product image | Public |

**Query Parameters (for GET):**
- `pageNumber` (default: 0)
- `pageSize` (default: 50)
- `sortBy` (default: "productId")
- `sortOrder` (default: "asc")

### Utility

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/echo?message=...` | Echo endpoint for testing |

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/signup` | Register a user and assign roles |
| `POST` | `/api/auth/signin` | Authenticate and receive a JWT |

### Cart, Addresses, and Orders

| Area | Example endpoints |
|------|-------------------|
| Cart | `POST /api/carts/products/{productId}/quantity/{quantity}`, `GET /api/carts/users/cart` |
| Addresses | `POST /api/addresses`, `GET /api/users/addresses`, `PUT /api/addresses/{addressId}` |
| Orders | `POST /api/order/users/payments/{paymentMethod}`, `GET /api/orders/users`, `GET /api/admin/orders` |

---

## Security Notes

JWT authentication is implemented through Spring Security, a `DaoAuthenticationProvider`, BCrypt password encoding, and a request filter that extracts Bearer tokens.

### Components

1. **JwtUtils** (`security/jwt/JwtUtils.java`)
   - Extracts JWT from `Authorization: Bearer` header
   - Generates JWT tokens from user details
   - Validates JWT tokens (signature, expiration)
   - Extracts username from JWT token

2. **AuthTokenFilter** (`security/jwt/AuthTokenFilter.java`)
   - Servlet filter that intercepts requests
   - Validates JWT tokens on each request
   - Sets authentication in Spring Security context
   - Loads user details and authorities

3. **AuthEntryPointJwt** (`security/jwt/AuthEntryPointJwt.java`)
   - Handles unauthorized access attempts
   - Returns JSON error responses (401 Unauthorized)

4. **DTOs**
   - `LoginRequest`: Username and password
   - `LoginResponse`: JWT token, username, and roles

### Configuration

JWT settings are configured in `application.properties`:
- `spring.app.jwtSecret`: Secret key for signing tokens
- `spring.app.jwtExpirationMs`: Token expiration time (default: 3600000ms = 1 hour)

### Important limitation

The security configuration currently permits `/api/admin/**` routes. Roles are stored and included in authentication responses, but role-based authorization rules have not yet been enforced. Do not expose this application publicly without completing access control and other production security work.

---

## Project Structure

```
src/main/java/com/ecommerce/project/
├── config/
│   ├── AppConfig.java              # ModelMapper bean configuration
│   └── AppConstants.java           # Pagination and sorting constants
├── controller/                     # Auth, catalog, cart, address, and order endpoints
├── exceptions/
│   ├── APIException.java           # Custom API exception
│   ├── MyGlobalExceptionHandler.java  # Global exception handler
│   └── ResourceNotFoundException.java # Resource not found exception
├── model/
│   ├── Address.java                # Address entity
│   ├── AppRole.java                # Role enum
│   ├── Category.java               # Category entity
│   ├── Product.java                # Product entity
│   ├── Cart.java / CartItem.java   # Shopping cart entities
│   ├── Order.java / OrderItem.java # Order entities
│   └── User.java / Role.java       # Authentication entities
├── payload/
│   ├── APIResponse.java            # Generic API response
│   ├── CategoryDTO.java            # Category data transfer object
│   ├── CategoryResponse.java       # Paginated category response
│   ├── ProductDTO.java             # Product data transfer object
│   └── ProductResponse.java        # Paginated product response
├── repositories/                   # Spring Data JPA repositories
├── security/
│   └── jwt/
│       ├── AuthEntryPointJwt.java  # Unauthorized handler
│       ├── AuthTokenFilter.java    # JWT authentication filter
│       ├── JwtUtils.java           # JWT utility methods
│       ├── LoginRequest.java       # Login request DTO
│       └── LoginResponse.java     # Login response DTO
├── service/                        # Business services and implementations
└── SbEcomApplication.java          # Spring Boot main class
```

---

## Run Locally

### Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- **PostgreSQL** (running locally or remote)

1. Create a PostgreSQL database:
```sql
CREATE DATABASE ecommerce;
```

2. Set the required environment variables:

```bash
export DB_USERNAME=your_postgres_user
export DB_PASSWORD=your_postgres_password
export JWT_SECRET=your_base64_encoded_jwt_secret
```

3. Build and start the application:

```bash
./mvnw clean test
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## Configuration

`application.properties` is local-only and ignored by Git. Copy the example file before running locally:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

The application reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and the optional `JWT_EXPIRATION_MS` from the environment. `application.properties.example` documents the expected property names without containing credentials.

## Current Limitations

- There is only a Spring context-load test; endpoint and repository behavior are not comprehensively tested.
- Admin routes are not role-protected yet.
- PostgreSQL must be available locally; no Docker Compose setup is included.
- Product images use local storage, so a deployment would need a durable object-storage strategy.

## Key Takeaways

- Building a layered Spring application with controllers, services, repositories, entities, and DTOs.
- Implementing JWT-based authentication and BCrypt password hashing.
- Modeling carts, orders, payments, addresses, products, and roles with JPA.
- Designing paginated REST endpoints and centralized error handling.

---

## License

No license has been specified for this repository.
