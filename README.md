# 📚 Spring Boot Starter Base Entity

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6.0+-blue.svg)](https://maven.apache.org/)
[![JitPack](https://jitpack.io/v/lqviet45/spring-boot-starter-base-entity.svg)](https://jitpack.io/#lqviet45/spring-boot-starter-base-entity)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A production-ready Spring Boot starter providing reusable base entities and repositories with **auditing**, **soft delete**, **UUIDv7 support**, and **20+ built-in repository methods** for JPA-based applications.

## 🚀 **Why Use This Library?**

- **⚡ Instant Productivity**: Get 20+ repository methods out of the box
- **🛡️ Production Ready**: Built-in auditing, soft delete, and optimistic locking
- **🆔 Flexible ID Types**: Support for Long, UUID, UUIDv7, and String IDs
- **🔍 Smart Queries**: Automatic filtering of deleted records
- **📊 Performance Optimized**: Time-ordered UUIDv7 for better database performance
- **🔧 Zero Configuration**: Works out of the box with Spring Security integration

## 📋 Table of Contents

- [🚀 Quick Start](#-quick-start)
- [🎯 Key Features](#-key-features)
- [💡 Usage Examples](#-usage-examples)
- [🆔 Entity Types](#-entity-types)
- [📖 Repository Methods](#-repository-methods)
- [⚙️ Configuration](#️-configuration)
- [🔧 Installation](#-installation)
- [📚 Documentation](#-documentation)
- [🤝 Contributing](#-contributing)

## 🚀 Quick Start

### 1. Add Dependency
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

### 2. Enable Base Entity
```java
@SpringBootApplication
@EnableBaseEntity  // 👈 Just add this annotation
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. Create Your Entity
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {  // 👈 Extend BaseEntity
    private String username;
    private String email;
    
    // Standard getters/setters...
}
```

### 4. Create Repository
```java
@Repository
public interface UserRepository extends BaseRepository<User> {
    // 👈 Inherits 20+ methods automatically!
}
```

### 5. Use in Service
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getActiveUsers() {
        return userRepository.findAllActive();  // ✨ Built-in method
    }
    
    public void softDeleteUser(Long id) {
        userRepository.softDeleteById(id);      // ✨ Built-in soft delete
    }
}
```

**🎉 That's it! You now have 20+ repository methods with auditing and soft delete!**

## 🎯 Key Features

### 🔍 **Built-in Repository Methods (20+)**
```java
// Find Operations
userRepository.findAllActive();              // Only active records
userRepository.findByIdNotDeleted(1L);       // Safe ID lookup
userRepository.findRecentlyCreated();        // Last 7 days
userRepository.findCreatedToday();           // Today's records

// Soft Delete Operations  
userRepository.softDeleteById(1L);           // Soft delete
userRepository.restoreById(1L);              // Restore deleted
userRepository.softDeleteByIds(Arrays.asList(1L, 2L, 3L));

// Count & Statistics
userRepository.countActive();                // Count active records
userRepository.countDeleted();               // Count deleted records
userRepository.existsByIdNotDeleted(1L);     // Safe existence check
```

### 🛡️ **Automatic Auditing**
```java
// Every entity automatically gets:
entity.getCreatedAt();        // When created
entity.getUpdatedAt();        // When last modified  
entity.getCreatedBy();        // Who created (Spring Security integration)
entity.getLastModifiedBy();   // Who last modified
entity.getVersion();          // Optimistic locking version
```

### 🗑️ **Smart Soft Delete**
```java
// Soft delete preserves data
user.markAsDeleted();                    // Mark as deleted
userRepository.findAllActive();          // Automatically excludes deleted
userRepository.findAllDeleted();         // Query deleted records
userRepository.restoreById(1L);          // Restore deleted record

// Utility methods for bulk operations
SoftDeleteUtils.filterActive(userList);  // Filter in memory
SoftDeleteUtils.markAsDeleted(userList); // Bulk soft delete
```

### 🆔 **Multiple ID Types**
```java
// Auto-incrementing Long ID (most common)
public class User extends BaseEntity { }

// Time-ordered UUID for distributed systems  
public class Order extends UuidBaseEntity { 
    public Order() {
        setId(UuidV7Utils.generate()); // Better performance than UUID.randomUUID()
    }
}

// String ID for natural keys
public class Country extends StringBaseEntity {
    public Country(String countryCode) {
        setId(countryCode); // e.g., "US", "CA", "GB"
    }
}
```

### ⚡ **UUIDv7 Performance Benefits**
```java
// Traditional UUID.randomUUID() - Random, poor database performance
UUID randomId = UUID.randomUUID(); // ❌ Causes index fragmentation

// UUIDv7 - Time-ordered, excellent database performance  
UUID timeOrderedId = UuidV7Utils.generate(); // ✅ Sequential, fast INSERTs

// Time-based queries are super efficient
Instant start = Instant.now().minusHours(1);
Instant end = Instant.now();
UuidV7Utils.TimeRange range = UuidV7Utils.createTimeRange(start, end);
// Use range.startUuid() and range.endUuid() in database queries
```

## 💡 Usage Examples

### 📊 **Complete CRUD Service Example**
```java
@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    // CREATE
    public User createUser(User user) {
        return userRepository.save(user);
        // ✨ Automatically sets: createdAt, updatedAt, createdBy, version
    }
    
    // READ (only active records)
    public Optional<User> getUser(Long id) {
        return userRepository.findByIdNotDeleted(id);
        // ✨ Automatically excludes soft-deleted records
    }
    
    public List<User> getAllActiveUsers() {
        return userRepository.findAllActive();
        // ✨ Only returns non-deleted records
    }
    
    // UPDATE  
    public User updateUser(User user) {
        return userRepository.save(user);
        // ✨ Automatically updates: updatedAt, lastModifiedBy, version
    }
    
    // SOFT DELETE (preserves data)
    public void deleteUser(Long id) {
        userRepository.softDeleteById(id);
        // ✨ Marks as deleted, doesn't remove from database
    }
    
    // RESTORE
    public void restoreUser(Long id) {
        userRepository.restoreById(id);
        // ✨ Restores soft-deleted record
    }
}
```

### 📈 **Analytics and Reporting**
```java
@Service
public class AnalyticsService {
    @Autowired
    private UserRepository userRepository;
    
    public UserStats getUserStatistics() {
        return UserStats.builder()
            .totalActive(userRepository.countActive())
            .totalDeleted(userRepository.countDeleted())
            .newToday(userRepository.findCreatedToday().size())
            .recentlyUpdated(userRepository.findRecentlyUpdated().size())
            .build();
    }
    
    public List<User> getNewUsersThisWeek() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findByCreatedAtBetween(weekAgo, now);
    }
}
```

### 🔧 **Admin Operations**
```java
@Service
public class AdminService {
    @Autowired 
    private UserRepository userRepository;
    
    // Bulk operations
    public void bulkDeleteUsers(List<Long> userIds) {
        int deletedCount = userRepository.softDeleteByIds(userIds);
        log.info("Soft deleted {} users", deletedCount);
    }
    
    public void bulkRestoreUsers(List<Long> userIds) {
        int restoredCount = userRepository.restoreByIds(userIds);
        log.info("Restored {} users", restoredCount);
    }
    
    // Data cleanup (run periodically)
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldDeletedRecords() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        int cleaned = userRepository.permanentlyDeleteOldRecords(sixMonthsAgo);
        log.info("Permanently deleted {} old records", cleaned);
    }
}
```

### 🔍 **Advanced Filtering with SoftDeleteUtils**
```java
@Service
public class UserFilterService {
    
    public List<User> processActiveUsers(List<User> allUsers) {
        // Filter active users in memory (useful for complex queries)
        List<User> activeUsers = SoftDeleteUtils.filterActive(allUsers);
        
        // Process only active users
        return activeUsers.stream()
            .filter(user -> user.isEmailVerified())
            .collect(toList());
    }
    
    public void archiveOldUsers() {
        List<User> allUsers = userRepository.findAll();
        LocalDateTime cutoff = LocalDateTime.now().minusYears(2);
        
        // Find users eligible for archival
        List<User> eligibleForArchival = SoftDeleteUtils
            .getEligibleForPermanentDeletion(allUsers, cutoff);
            
        // Archive and then permanently delete
        archiveUsers(eligibleForArchival);
        eligibleForArchival.forEach(user -> 
            userRepository.delete(user)); // Hard delete
    }
}
```

## 🆔 Entity Types

### 1. **BaseEntity (Auto-incrementing Long ID)**
```java
@Entity
public class User extends BaseEntity {
    private String username;
    // ID is auto-generated: 1, 2, 3, 4...
}
```
**✅ Best for**: Traditional single-database applications, internal entities  
**✅ Performance**: Excellent for primary keys, fast JOINs  
**✅ Use when**: Standard web applications, single database

### 2. **UuidBaseEntity (UUID ID)**
```java
@Entity  
public class Order extends UuidBaseEntity {
    private String orderNumber;
    
    public Order() {
        setId(UuidV7Utils.generate()); // Time-ordered UUID
    }
}
```
**✅ Best for**: Distributed systems, microservices, external APIs  
**✅ Performance**: UUIDv7 provides time-ordering for better database performance  
**✅ Use when**: Multi-database systems, need globally unique IDs

### 3. **StringBaseEntity (String ID)**
```java
@Entity
public class Country extends StringBaseEntity {
    private String name;
    
    public Country(String countryCode) {
        setId(countryCode); // "US", "CA", "GB"
    }
}
```
**✅ Best for**: Natural business keys, human-readable IDs  
**✅ Performance**: Good when IDs are short and normalized  
**✅ Use when**: Country codes, product SKUs, user handles

## 📖 Repository Methods

### 🔍 **Find Operations**
| Method | Description | Example |
|--------|-------------|---------|
| `findAllActive()` | All non-deleted records | `userRepo.findAllActive()` |
| `findByIdNotDeleted(id)` | Find by ID (active only) | `userRepo.findByIdNotDeleted(1L)` |
| `findAllDeleted()` | All soft-deleted records | `userRepo.findAllDeleted()` |
| `findRecentlyCreated()` | Created in last 7 days | `userRepo.findRecentlyCreated()` |
| `findCreatedToday()` | Created today | `userRepo.findCreatedToday()` |
| `findByCreatedAtBetween()` | Created in date range | `userRepo.findByCreatedAtBetween(start, end)` |

### 🗑️ **Soft Delete Operations**
| Method | Description | Returns |
|--------|-------------|---------|
| `softDeleteById(id)` | Soft delete by ID | `int` (affected rows) |
| `restoreById(id)` | Restore soft-deleted record | `int` (affected rows) |
| `softDeleteByIds(ids)` | Bulk soft delete | `int` (affected rows) |
| `restoreByIds(ids)` | Bulk restore | `int` (affected rows) |

### 📊 **Count & Statistics**
| Method | Description | Returns |
|--------|-------------|---------|
| `countActive()` | Count active records | `long` |
| `countDeleted()` | Count deleted records | `long` |
| `existsByIdNotDeleted(id)` | Check if active record exists | `boolean` |
| `isSoftDeleted(id)` | Check if record is deleted | `boolean` |

### 🔧 **Type-Specific Methods**

#### **UuidBaseRepository Additional Methods**
```java
// Find by UUID string
Optional<Order> order = orderRepo.findByIdStringNotDeleted("550e8400-...");

// Find by multiple UUID strings  
List<Order> orders = orderRepo.findByIdStringsNotDeleted(uuidStringList);
```

#### **StringBaseRepository Additional Methods**
```java
// Pattern matching
List<Country> countries = countryRepo.findByIdLikeIgnoreCase("%US%");

// Prefix/suffix search
List<Country> countries = countryRepo.findByIdStartingWith("US");
List<Country> countries = countryRepo.findByIdEndingWith("A");

// Length-based search
List<Country> isoCodes = countryRepo.findByIdLength(2); // ISO country codes
```

## ⚙️ Configuration

### **Application Properties**
```properties
# Base entity settings
base-entity.auditing.enabled=true
base-entity.soft-delete.enabled=true
base-entity.cleanup.enabled=false
base-entity.cleanup.retention-period=P6M

# JPA performance optimizations (recommended)
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true

# Logging
logging.level.com.lqviet.baseentity=INFO
```

### **Custom Auditor Provider**
```java
@Configuration
public class CustomAuditConfig {
    
    @Bean
    @Primary
    public AuditorAware<String> customAuditorProvider() {
        return () -> {
            // Your custom logic to get current user
            String currentUser = getCurrentUserFromCustomContext();
            return Optional.ofNullable(currentUser);
        };
    }
}
```

### **Database Schema**
Each entity automatically gets these fields:
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,           -- Auto-generated
    created_at TIMESTAMP NOT NULL,      -- Automatic
    updated_at TIMESTAMP NOT NULL,      -- Automatic  
    created_by VARCHAR(100),            -- From Spring Security
    last_modified_by VARCHAR(100),      -- From Spring Security
    version BIGINT,                     -- Optimistic locking
    is_deleted BOOLEAN DEFAULT FALSE,   -- Soft delete flag
    -- Your custom fields...
    username VARCHAR(50),
    email VARCHAR(100)
);
```

## 🔧 Installation

### **Maven**
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

### **Gradle**
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.lqviet45:spring-boot-starter-base-entity:v1.1.0'
}
```

### **Build from Source** 
```bash
git clone https://github.com/lqviet45/spring-boot-starter-base-entity.git
cd spring-boot-starter-base-entity
mvn clean install
```

## 📚 Documentation

### **Detailed Repository Guide**
For a complete reference of all repository methods, see: [Repository Methods Guide](Repository%20guide.md)

### **Database Compatibility**
- ✅ **PostgreSQL** (Recommended)
- ✅ **MySQL/MariaDB** 
- ✅ **SQL Server**
- ✅ **Oracle**
- ✅ **H2** (Testing)
- ✅ **HSQLDB** (Testing)

### **Spring Boot Compatibility**
- ✅ **Spring Boot 3.x** (Recommended)
- ✅ **Spring Boot 2.7+** (Legacy support)
- ✅ **Java 17+** (Required)
- ✅ **Java 21+** (Recommended)

### **Performance Tips**

1. **Use Pagination for Large Datasets**
```java
Page<User> users = userRepository.findAllActive(PageRequest.of(0, 20));
```

2. **Batch Operations are More Efficient**
```java
// ✅ Good - single query
userRepository.softDeleteByIds(Arrays.asList(1L, 2L, 3L));

// ❌ Bad - multiple queries  
ids.forEach(id -> userRepository.softDeleteById(id));
```

3. **Filter at Database Level**
```java
// ✅ Good - database filtering
List<User> users = userRepository.findAllActive();

// ❌ Bad - memory filtering
List<User> users = userRepository.findAll().stream()
    .filter(u -> !u.isDeleted())
    .collect(toList());
```

## 🤝 Contributing

### **Development Setup**
```bash
git clone https://github.com/lqviet45/spring-boot-starter-base-entity.git
cd spring-boot-starter-base-entity
mvn clean install
mvn test
```

### **Contributing Guidelines**
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Write tests for new functionality
4. Ensure all tests pass: `mvn test`
5. Submit a Pull Request

### **Reporting Issues**
- Use [GitHub Issues](https://github.com/lqviet45/spring-boot-starter-base-entity/issues)
- Include reproduction steps and environment details
- Provide sample code when possible

---

## 📄 License

Apache 2.0 License - see [LICENSE](LICENSE) file for details.

<div align="center">

**⭐ Star this repository if it saved you development time!**

[🐛 Report Bug](https://github.com/lqviet45/spring-boot-starter-base-entity/issues) • [✨ Request Feature](https://github.com/lqviet45/spring-boot-starter-base-entity/issues) • [🤝 Contribute](https://github.com/lqviet45/spring-boot-starter-base-entity/pulls)

</div>
