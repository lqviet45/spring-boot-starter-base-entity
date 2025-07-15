Spring Boot Starter Base Entity
Overview
The Spring Boot Starter Base Entity is a Spring Boot starter that provides a foundation for JPA entities with built-in support for auditing and soft delete functionality. It simplifies the development of Spring Boot applications by offering a reusable BaseEntity class, a BaseRepository interface, and utility classes for entity management, along with auto-configuration for seamless integration.
This starter is ideal for developers who need a standardized way to implement entity auditing (creation and modification tracking), soft delete functionality, and batch operations in their Spring Boot applications.
Features

BaseEntity: An abstract class with common fields for all entities:
id: Auto-generated unique identifier (Long).
createdAt: Timestamp of entity creation.
updatedAt: Timestamp of the last update.
createdBy: User who created the entity.
lastModifiedBy: User who last modified the entity.
version: Optimistic locking support.
isDeleted: Soft delete flag.
Methods: markAsDeleted(), restore(), isDeleted(), isNew().


BaseRepository: A generic JPA repository interface with:
CRUD operations for active (non-deleted) and deleted entities.
Soft delete and restore operations by ID or in bulk.
Queries for filtering by creation/update timestamps, creator, or modifier.
Pagination support for active and deleted entities.
Convenience methods for finding recently created/updated entities or entities created/updated today.


SoftDeleteUtils: Utility class for soft delete operations:
Filter active or deleted entities.
Bulk soft delete or restore.
Check eligibility for soft delete or permanent deletion.


Auditing Support: Automatically tracks createdBy and lastModifiedBy with Spring Security or custom user context.
Auto-Configuration: Automatically enables JPA auditing and repositories via @EnableBaseEntity or auto-configuration.
Configurable Properties: Customize auditing, soft delete, and cleanup behavior via application.properties.

Prerequisites

Java 21 or later
Maven 3.6.0 or later
Spring Boot 3.5.3 or later

Installation
The library is available via JitPack. Add the following to your pom.xml:
Maven
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.lqviet45</groupId>
    <artifactId>spring-boot-starter-base-entity</artifactId>
    <version>1.0.0</version>
</dependency>

Gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.lqviet45:spring-boot-starter-base-entity:1.0.0'
}

Replace 1.0.0 with the desired release tag or commit hash from the GitHub repository.
Usage
1. Enable Base Entity
Add the @EnableBaseEntity annotation to a configuration class or rely on auto-configuration:
package com.example.config;

import com.lqviet.baseentity.annotations.EnableBaseEntity;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBaseEntity
public class AppConfig {
}

2. Create an Entity
Extend the BaseEntity class for your entity:
package com.example.entities;

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

3. Create a Repository
Extend the BaseRepository interface for your entity:
package com.example.repositories;

import com.example.entities.User;
import com.lqviet.baseentity.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User> {
}

4. Configure Auditing
The starter auto-configures JPA auditing. For Spring Security integration, ensure spring-security is in your classpath. The AuditingConfig provides:

SpringSecurityAuditorAware: Uses Spring Security's authentication context for createdBy and lastModifiedBy.
DefaultAuditorAware: Fallback to "system" or a custom user context if Spring Security is unavailable.

To customize the auditor, override the auditorProvider bean:
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class CustomAuditorConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("custom-user");
    }
}

5. Use the Repository and Utilities
Inject the repository and use its methods or SoftDeleteUtils for operations:
package com.example.services;

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

    public void createUser(User user) {
        userRepository.save(user);
    }

    public void softDeleteUser(Long id) {
        userRepository.softDeleteById(id);
    }

    public List<User> getActiveUsers() {
        return userRepository.findAllActive();
    }

    public List<User> filterActiveUsers(List<User> users) {
        return SoftDeleteUtils.filterActive(users);
    }

    public void permanentlyDeleteOldUsers(LocalDateTime cutoffDate) {
        List<User> deletedUsers = userRepository.findAllDeleted();
        List<User> toDelete = SoftDeleteUtils.getEligibleForPermanentDeletion(deletedUsers, cutoffDate);
        userRepository.permanentlyDeleteOldRecords(cutoffDate);
    }
}

Configuration
Configure the starter in application.properties or application.yml:
# Enable/disable auditing and soft delete
base-entity.auditing.enabled=true
base-entity.soft-delete.enabled=true

# Configure cleanup of soft-deleted records
base-entity.cleanup.enabled=false
base-entity.cleanup.retention-period=P6M

# JPA optimizations
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true

# Logging
logging.level.com.lqviet.baseentity=INFO


base-entity.cleanup.retention-period: ISO-8601 duration (e.g., P6M for 6 months) for retaining soft-deleted records before permanent deletion.
Adjust spring.jpa.properties.hibernate.dialect and spring.datasource.url for your database (e.g., MySQL, PostgreSQL).

Soft Delete Functionality

Soft Delete: Use softDeleteById(id), softDelete(entity), or SoftDeleteUtils.markAsDeleted(entities) to mark entities as deleted.
Restore: Use restoreById(id), restore(entity), or SoftDeleteUtils.restore(entities) to restore entities.
Filter: Use findAllActive(), findByIdNotDeleted(id), or SoftDeleteUtils.filterActive(entities) for non-deleted entities; use findAllDeleted() or SoftDeleteUtils.filterDeleted(entities) for deleted entities.
Permanent Deletion: Use permanentlyDeleteOldRecords(cutoffDate) or SoftDeleteUtils.getEligibleForPermanentDeletion(entities, cutoffDate) for cleanup.

Releasing with JitPack
To release the library using JitPack:

Push to GitHub: Ensure your code is pushed to the GitHub repository.
Create a Release:
Go to the repository on GitHub.
Create a new release with a tag (e.g., v1.0.0).
JitPack will automatically build the artifact from the tagged release.


Verify on JitPack:
Visit https://jitpack.io/#lqviet45/spring-boot-starter-base-entity.
Check the build status and ensure the artifact is available.


Use the Artifact: Reference the dependency as shown in the Installation section, using the release tag or commit hash.

No additional configuration is needed for JitPack, as it builds directly from your GitHub repository. Ensure your pom.xml is correctly configured (as provided) for Maven builds.
Building from Source

Clone the repository:git clone https://github.com/lqviet45/spring-boot-starter-base-entity.git


Navigate to the project directory:cd spring-boot-starter-base-entity


Build the project:mvn clean install



License
This project is licensed under the Apache License, Version 2.0.
Contributing
Contributions are welcome! Submit a pull request or open an issue on the GitHub repository.
Contact
For questions or feedback, contact Le Quoc Viet at lqviet455@gmail.com.
