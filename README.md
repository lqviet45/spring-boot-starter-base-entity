# 📚 Spring Boot Starter Base Entity

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6.0+-blue.svg)](https://maven.apache.org/)
[![Build Status](https://img.shields.io/badge/Build-Passing-green.svg)](#)

A Spring Boot starter providing reusable base entities and repositories with auditing, soft delete, and UUIDv7 support for JPA-based applications.

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
    <version>v1.1.0</version>
</dependency>

# Clone the repository (for development)
git clone https://github.com/lqviet45/spring-boot-starter-base-entity.git
cd spring-boot-starter-base-entity

# Build and install
mvn clean install
```

## 🏗️ Features

- **Base Entities**: Abstract JPA entities (`BaseEntity`, `UuidBaseEntity`, `StringBaseEntity`) with fields for ID (Long/UUID/String), creation/update timestamps, creator/modifier tracking, optimistic locking, and soft delete support.
- **Repositories**: Generic JPA repositories (`BaseRepository`, `UuidBaseRepository`, `StringBaseRepository`) with CRUD operations, soft delete/restore, and type-specific queries.
- **SoftDeleteUtils**: Utility methods for filtering, soft deleting, restoring, and managing permanent deletion of entities.
- **UuidV7Utils**: Generates time-ordered UUIDv7 IDs for improved database performance and time-based queries.
- **Auditing**: Tracks `createdBy` and `lastModifiedBy` with Spring Security or a fallback "system" user.
- **Auto-Configuration**: Enables JPA auditing and repositories via `@EnableBaseEntity` or auto-configuration.
- **Configurable**: Customize auditing, soft delete, and cleanup via properties.

## ⚡ Implementation

### 🟢 Implemented Features
- Base entities with auditing fields (`id`, `createdAt`, `updatedAt`, `createdBy`, `lastModifiedBy`, `version`, `isDeleted`).
- Repositories for Long, UUID, and String IDs with soft delete, restore, and filtered queries (e.g., `findAllActive()`, `findByIdStringNotDeleted()`).
- `SoftDeleteUtils` for bulk operations and filtering.
- `UuidV7Utils` for generating and managing time-ordered UUIDv7 IDs.
- Auto-configured JPA auditing with Spring Security support.
- Property-based configuration for auditing and soft delete.

### 🟡 In Development
- Additional query optimizations for large datasets.
- Enhanced UUIDv7 integration with database-specific features.

### 🔴 Planned Features
- Custom query builder for advanced filtering.
- Integration with Spring Batch for bulk operations.
- Support for NoSQL databases (e.g., MongoDB).
- Advanced auditing with custom metadata.

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
- For Details information about the repository please go to [Repository guide](https://github.com/lqviet45/spring-boot-starter-base-entity/blob/main/Repository%20guide.md)

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
    <version>v1.1.0</version>
</dependency>
```

For Gradle:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.lqviet45:spring-boot-starter-base-entity:v1.1.0'
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
For UUID-based entities:
```java
import com.lqviet.baseentity.entities.UuidBaseEntity;
import com.lqviet.baseentity.utils.UuidV7Utils;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends UuidBaseEntity {
    private String username;
    private String email;

    public User() {
        setId(UuidV7Utils.generate());
    }
}
```

### Create a Repository
```java
import com.example.entities.User;
import com.lqviet.baseentity.repository.UuidBaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends UuidBaseRepository<User> {
}
```

### Use in Services
```java
import com.example.entities.User;
import com.example.repositories.UserRepository;
import com.lqviet.baseentity.utils.SoftDeleteUtils;
import com.lqviet.baseentity.utils.UuidV7Utils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void softDeleteUser(UUID id) {
        userRepository.softDeleteById(id);
    }

    public List<User> getActiveUsers() {
        return userRepository.findAllActive();
    }

    public List<User> filterActive(List<User> users) {
        return SoftDeleteUtils.filterActive(users);
    }

    public List<User> findUsersInTimeRange(Instant start, Instant end) {
        UuidV7Utils.TimeRange range = UuidV7Utils.createTimeRange(start, end);
        return userRepository.findByIdIn(List.of(range.startUuid(), range.endUuid()));
    }
}
```

### Example Operations
```
// Generate UUIDv7
UUID id = UuidV7Utils.generate();
user.setId(id);

// Soft delete
userRepository.softDeleteById(id);

// Restore
userRepository.restoreById(id);

// Filter active entities
List<User> activeUsers = SoftDeleteUtils.filterActive(userRepository.findAll());

// Time-based query
Instant start = Instant.now().minusSeconds(3600);
Instant end = Instant.now();
List<User> recentUsers = userService.findUsersInTimeRange(start, end);

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
│   │   │       ├── entities/          # BaseEntity, UuidBaseEntity, StringBaseEntity
│   │   │       ├── repository/        # CommonBaseRepository, BaseRepository, UuidBaseRepository, StringBaseRepository
│   │   │       └── utils/             # SoftDeleteUtils, UuidV7Utils
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
- **Auditing**: Tracks `createdBy` and `lastModifiedBy` using Spring Security (if available) or a "system" fallback.
- **Custom Auditor**: Override the `auditorProvider` bean for custom auditing logic.

## 🎯 Releasing with JitPack

1. Push code to [GitHub](https://github.com/lqviet45/spring-boot-starter-base-entity).
2. Create a release with a tag (e.g., `v1.1.0`) on GitHub.
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


Apache 2 License

Copyright (c) 2025 Le Quoc Viet

<div align="center">

**⭐ Star this repository if it helped you!**

[Report Bug](https://github.com/lqviet45/spring-boot-starter-base-entity/issues) • [Request Feature](https://github.com/lqviet45/spring-boot-starter-base-entity/issues) • [Contribute](https://github.com/lqviet45/spring-boot-starter-base-entity/pulls)

</div>
