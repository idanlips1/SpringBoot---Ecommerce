# sb-ecom — Spring Boot E-commerce API

> **Status:** 🚧 Currently under active development

A RESTful e-commerce API built with Spring Boot, featuring category and product management, JWT-based security scaffolding, and comprehensive error handling.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Domain Models](#domain-models)
- [API Endpoints](#api-endpoints)
- [Security Module](#security-module)
- [Project Structure](#project-structure)
- [Setup & Configuration](#setup--configuration)
- [Development Notes](#development-notes)

---

## Overview

This project is a work-in-progress e-commerce backend API that provides endpoints for managing categories and products. The application includes pagination, sorting, search functionality, file upload capabilities, and a foundation for JWT-based authentication.

**Current Focus:** Core CRUD operations for categories and products, with security infrastructure being developed.

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

## Features

### ✅ Implemented

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

- **Error Handling**
  - Global exception handler
  - Custom exceptions (`APIException`, `ResourceNotFoundException`)
  - Validation error responses

- **Security Infrastructure** (Scaffolding)
  - JWT utility classes
  - Authentication token filter
  - Unauthorized entry point handler
  - Login request/response DTOs

### 🚧 In Progress

- Complete security configuration (SecurityFilterChain, UserDetailsService)
- Authentication endpoints (login, register)
- Password encoding
- Role-based access control implementation

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

---

## Security Module

The security module currently contains the foundational components for JWT-based authentication:

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

### Status

⚠️ **Security is not fully wired yet.** The following are still needed:
- Spring Security configuration class (`SecurityFilterChain`)
- `UserDetailsService` implementation
- Password encoder bean
- Authentication controller (login/register endpoints)
- Integration of `AuthTokenFilter` into the security filter chain

---

## Project Structure

```
src/main/java/com/ecommerce/project/
├── config/
│   ├── AppConfig.java              # ModelMapper bean configuration
│   └── AppConstants.java           # Pagination and sorting constants
├── controller/
│   ├── CategoryController.java     # Category REST endpoints
│   └── ProductController.java      # Product REST endpoints
├── exceptions/
│   ├── APIException.java           # Custom API exception
│   ├── MyGlobalExceptionHandler.java  # Global exception handler
│   └── ResourceNotFoundException.java # Resource not found exception
├── model/
│   ├── Address.java                # Address entity
│   ├── AppRole.java                # Role enum
│   ├── Category.java               # Category entity
│   ├── Product.java                # Product entity
│   ├── Role.java                   # Role entity
│   └── User.java                   # User entity
├── payload/
│   ├── APIResponse.java            # Generic API response
│   ├── CategoryDTO.java            # Category data transfer object
│   ├── CategoryResponse.java       # Paginated category response
│   ├── ProductDTO.java             # Product data transfer object
│   └── ProductResponse.java        # Paginated product response
├── repositories/
│   ├── CategoryRepository.java     # Category JPA repository
│   └── ProductRepository.java      # Product JPA repository
├── security/
│   └── jwt/
│       ├── AuthEntryPointJwt.java  # Unauthorized handler
│       ├── AuthTokenFilter.java    # JWT authentication filter
│       ├── JwtUtils.java           # JWT utility methods
│       ├── LoginRequest.java       # Login request DTO
│       └── LoginResponse.java     # Login response DTO
├── service/
│   ├── CategoryService.java        # Category service interface
│   ├── CategoryServiceImpl.java   # Category service implementation
│   ├── FileService.java            # File service interface
│   ├── FileServiceImpl.java        # File service implementation
│   ├── ProductService.java         # Product service interface
│   └── ProductServiceImpl.java     # Product service implementation
└── SbEcomApplication.java          # Spring Boot main class
```

---

## Setup & Configuration

### Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- **PostgreSQL** (running locally or remote)

### Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE ecommerce;
```

2. Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Running the Application

1. Clone the repository:
```bash
git clone <repository-url>
cd sb-ecom
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Configuration Files

⚠️ **Important:** `src/main/resources/application.properties` is git-ignored to protect sensitive configuration. Create your own `application.properties` file with the following structure:

```properties
# Application
spring.application.name=sb-ecom

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
spring.app.jwtSecret=your_base64_encoded_secret_key_here
spring.app.jwtExpirationMs=3600000

# Logging
logging.level.org.springframework=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.com.ecommerce.project=DEBUG
```

---

## Development Notes

### Current Status

- ✅ Core CRUD operations for categories and products
- ✅ Pagination and sorting
- ✅ Search functionality (by category and keyword)
- ✅ File upload for product images
- ✅ Global exception handling
- ✅ Request validation
- 🚧 Security configuration (JWT utilities ready, but not fully integrated)
- 🚧 User authentication endpoints
- 🚧 Role-based access control

### Next Steps

1. Complete security configuration:
   - Implement `SecurityFilterChain`
   - Create `UserDetailsService` implementation
   - Add password encoder
   - Create authentication controller

2. Add user management:
   - Registration endpoint
   - Login endpoint
   - User profile management

3. Enhance product features:
   - Product reviews/ratings
   - Inventory management
   - Product variants

4. Add order management:
   - Shopping cart
   - Order creation and tracking
   - Payment integration

### Known Issues

- Security filter chain not configured (JWT filter not active)
- No authentication endpoints yet
- Product image upload path needs configuration

---

## License

TBD

---

**Note:** This project is actively being developed. Features and APIs may change. Check the commit history for the latest updates.
