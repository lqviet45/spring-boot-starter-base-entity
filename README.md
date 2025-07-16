# 📚 Spring Boot Starter Base Entity

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6.0+-blue.svg)](https://maven.apache.org/)
[![Build Status](https://img.shields.io/badge/Build-Passing-green.svg)](#)

A Spring Boot starter providing a reusable base entity and repository with auditing and soft delete functionality for JPA-based applications.

## 📋 Table of Contents

- [🚀 Quick Start](#-quick-start)
- [🏗️ Features](#️-features)
- [⚡ Implementation](#-implementation)
- [🛠️ Tech Stack](#️-tech-stack)
- [📦 Prerequisites](#-prerequisites)
- [🔧 Installation & Setup](#-installation--setup)
- [🌐 Usage](#-usage)
- [🗂️ Project Structure](#️-project-structure)
- [🔐 Configuration](#-configuration)
- [🎯 Releasing with JitPack](#-releasing-with-jitpack)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

## 🚀 Quick Start

```bash
# Add to pom.xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.lqviet45</groupId>
    <artifactId>spring-boot-starter-base-entity</artifactId>
    <version>v1.0.0</version>
</dependency>

# Clone the repository (for development)
git clone https://github.com/lqviet45/spring-boot-starter-base-entity.git
cd spring-boot-starter-base-entity

# Build and install
mvn clean install
```

## 🏗️ Features

- **BaseEntity**: Abstract JPA entity with fields for ID, creation/update timestamps, creator/modifier tracking, optimistic locking, and soft delete support.
- **BaseRepository**: Generic JPA repository with CRUD operations, soft delete/restore, and queries for active/deleted entities.
- **SoftDeleteUtils**: Utility methods for filtering, soft deleting, restoring, and managing permanent deletion of entities.
- **Auditing**: Tracks `createdBy` and `lastModifiedBy` with Spring Security or a fallback "system" user.
- **Auto-Configuration**: Enables JPA auditing and repositories via `@EnableBaseEntity` or auto-configuration.
- **Configurable**: Customize auditing, soft delete, and cleanup via properties.

## ⚡ Implementation

### 🟢 Implemented Features
- `BaseEntity` with auditing fields (`id`, `createdAt`, `updatedAt`, `createdBy`, `lastModifiedBy`, `version`, `isDeleted`).
- `BaseRepository` with soft delete, restore, and filtered queries (e.g., `findAllActive()`, `findByCreatedAtBetween()`).
- `SoftDeleteUtils` for bulk operations and filtering.
- Auto-configured JPA auditing with Spring Security support.
- Property-based configuration for auditing and soft delete.

### 🟡 In Development
- Support for additional ID types in `BaseRepository` (currently supports `Long`).

### 🔴 Planned Features
- Custom query builder for advanced filtering.
- Integration with Spring Batch for bulk operations.
- Support for NoSQL databases (e.g., MongoDB).
- Enhanced auditing with custom metadata.

## 🛠️ Tech Stack

| Category             | Technology                     |
|----------------------|--------------------------------|
| **Backend Framework** | Spring Boot 3.5.3             |
| **ORM**              | JPA/Hibernate                 |
| **Security**         | Spring Security (optional)    |
| **Build Tool**       | Maven                         |
| **Java Version**     | 21+                           |
| **Dependencies**     | Lombok, H2 (test), Validation |

## 📦 Prerequisites

- **Java 21+** - [Download OpenJDK](https://openjdk.java.net/)
- **Maven 3.6+** - [Installation Guide](https://maven.apache.org/install.html)
- **Database** - Any JPA-compatible database (e.g., PostgreSQL, MySQL, H2 for testing)

## 🔧 Installation & Setup

### 1. Add Dependency
For Maven:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.lqviet45</groupId>
    <artifactId>spring-boot-starter-base-entity</artifactId>
    <version>v1.0.0</version>
</dependency>
```

For Gradle:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.lqviet45:spring-boot-starter-base-entity:v1.0.0'
}
```

### 2. Configure Database
Add to `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/yourdb
spring.datasource.username=admin
spring.datasource.password=secret
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 3. Build Locally (Optional)
```bash
git clone https://github.com/lqviet45/spring-boot-starter-base-entity.git
cd spring-boot-starter-base-entity
mvn clean install
```

## 🌐 Usage

### Enable Base Entity
```java
import com.lqviet.baseentity.annotations.EnableBaseEntity;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBaseEntity
public class AppConfig {
}
```

### Define an Entity
```java
import com.lqviet.baseentity.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {
    private String username;
    private String email;
}
```

### Create a Repository
```java
import com.example.entities.User;
import com.lqviet.baseentity.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User> {
}
```

### Use in Services
```java
import com.example.entities.User;
import com.example.repositories.UserRepository;
import com.lqviet.baseentity.utils.SoftDeleteUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void softDeleteUser(Long id) {
        userRepository.softDeleteById(id);
    }

    public List<User> getActiveUsers() {
        return userRepository.findAllActive();
    }

    public List<User> filterActive(List<User> users) {
        return SoftDeleteUtils.filterActive(users);
    }
}
```

### Example Operations
```java
// Soft delete
userRepository.softDeleteById(1L);

// Restore
userRepository.restoreById(1L);

// Filter active entities
List<User> activeUsers = SoftDeleteUtils.filterActive(userRepository.findAll());

// Permanent deletion
LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
userRepository.permanentlyDeleteOldRecords(cutoff);
```

## 🗂️ Project Structure

```
spring-boot-starter-base-entity/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/lqviet/baseentity/
│   │   │       ├── annotations/       # EnableBaseEntity annotation
│   │   │       ├── config/            # Auto-configuration and auditing
│   │   │       ├── entities/          # BaseEntity class
│   │   │       ├── repository/        # BaseRepository interface
│   │   │       └── utils/             # SoftDeleteUtils
│   │   └── resources/
│   │       └── application.properties # Default configuration
│   └── test/                         # Unit and integration tests
├── LICENSE                           # Apache 2.0 License
├── pom.xml                           # Maven configuration
└── README.md                         # Project documentation
```

## 🔐 Configuration

### Application Properties
```properties
# Base entity settings
base-entity.auditing.enabled=true
base-entity.soft-delete.enabled=true
base-entity.cleanup.enabled=false
base-entity.cleanup.retention-period=P6M

# JPA optimizations
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true

# Logging
logging.level.com.lqviet.baseentity=INFO
```

### Security
- **Auditing**: Automatically tracks `createdBy` and `lastModifiedBy` using Spring Security (if available) or a "system" fallback.
- **Custom Auditor**: Override the `auditorProvider` bean for custom auditing logic.

## 🎯 Releasing with JitPack

1. Push code to [GitHub](https://github.com/lqviet45/spring-boot-starter-base-entity).
2. Create a release with a tag (e.g., `v1.0.0`) on GitHub.
3. Verify build status at [jitpack.io](https://jitpack.io/#lqviet45/spring-boot-starter-base-entity).
4. Use the tagged version in your project (see [Installation](#-installation--setup)).

## 🤝 Contributing

### 1. Fork & Clone
```bash
git clone https://github.com/your-username/spring-boot-starter-base-entity.git
cd spring-boot-starter-base-entity
```

### 2. Create Feature Branch
```bash
git checkout -b feature/your-feature-name
```

### 3. Development Guidelines
- Follow [Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-introduction.html).
- Write unit tests for new features.
- Update documentation for changes.
- Ensure tests pass: `mvn test`.

### 4. Submit Pull Request
- Push changes to your fork.
- Create a Pull Request with a clear description.
- Ensure CI checks pass.

### Reporting Issues
- Use [GitHub Issues](https://github.com/lqviet45/spring-boot-starter-base-entity/issues).
- Include reproduction steps, environment details, and logs.

## 📄 License

This project is licensed under the Apache 2 License - see the [LICENSE](LICENSE) file for details.

```
Apache 2 License

Copyright (c) 2025 Le Quoc Viet

<div align="center">

**⭐ Star this repository if it helped you!**

[Report Bug](https://github.com/lqviet45/spring-boot-starter-base-entity/issues) • [Request Feature](https://github.com/lqviet45/spring-boot-starter-base-entity/issues) • [Contribute](https://github.com/lqviet45/spring-boot-starter-base-entity/pulls)
```
