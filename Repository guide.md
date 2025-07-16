# 📋 Repository Methods Quick Reference

## 🔍 Find Operations

### Basic Find Methods
```java
// Find all active (non-deleted) records
List<User> users = userRepository.findAllActive();
Page<User> users = userRepository.findAllActive(pageable);

// Find by ID (only active records)
Optional<User> user = userRepository.findByIdNotDeleted(1L);

// Find all soft-deleted records
List<User> deletedUsers = userRepository.findAllDeleted();
Page<User> deletedUsers = userRepository.findAllDeleted(pageable);

// Find by multiple IDs
List<User> users = userRepository.findByIdIn(Arrays.asList(1L, 2L, 3L));
```

### Date-Based Queries
```java
// Find by creation date range
LocalDateTime start = LocalDateTime.now().minusDays(30);
LocalDateTime end = LocalDateTime.now();
List<User> users = userRepository.findByCreatedAtBetween(start, end);

// Find by update date range
List<User> users = userRepository.findByUpdatedAtBetween(start, end);

// Find recently created (last 7 days)
List<User> recentUsers = userRepository.findRecentlyCreated();

// Find recently updated (last 7 days)
List<User> updatedUsers = userRepository.findRecentlyUpdated();

// Find created today
List<User> todayUsers = userRepository.findCreatedToday();

// Find updated today
List<User> updatedToday = userRepository.findUpdatedToday();
```

### Audit-Based Queries
```java
// Find by creator
List<User> users = userRepository.findByCreatedBy("admin");

// Find by last modifier
List<User> users = userRepository.findByLastModifiedBy("admin");
```

## 🗑️ Soft Delete Operations

### Single Record Operations
```java
// Soft delete by ID
int deleted = userRepository.softDeleteById(1L);

// Restore soft-deleted record
int restored = userRepository.restoreById(1L);

// Soft delete entity directly
boolean success = userRepository.softDelete(user);

// Restore entity directly
boolean success = userRepository.restore(user);
```

### Bulk Operations
```java
// Soft delete multiple records by IDs
List<Long> ids = Arrays.asList(1L, 2L, 3L);
int deletedCount = userRepository.softDeleteByIds(ids);

// Restore multiple records by IDs
int restoredCount = userRepository.restoreByIds(ids);

// Permanently delete old soft-deleted records
LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
int permanentlyDeleted = userRepository.permanentlyDeleteOldRecords(cutoff);
```

## 📊 Count & Check Operations

### Count Methods
```java
// Count active records
long activeCount = userRepository.countActive();

// Count soft-deleted records
long deletedCount = userRepository.countDeleted();

// Count all records (active + deleted)
long totalCount = userRepository.countTotal();
```

### Existence Checks
```java
// Check if active record exists
boolean exists = userRepository.existsByIdNotDeleted(1L);

// Check if any records exist by creator
boolean exists = userRepository.existsByCreatedBy("admin");

// Check if record is soft-deleted
boolean isDeleted = userRepository.isSoftDeleted(1L);
```

## 🆔 ID Type-Specific Methods

### Long ID (BaseRepository)
```java
// Find by ID range
List<User> users = userRepository.findByIdRange(1L, 100L);
```

### UUID ID (UuidBaseRepository)
```java
// Find by UUID string
Optional<Order> order = orderRepository.findByIdStringNotDeleted("550e8400-e29b-41d4-a716-446655440000");

// Find by multiple UUID strings
List<String> uuidStrings = Arrays.asList("uuid1", "uuid2");
List<Order> orders = orderRepository.findByIdStringsNotDeleted(uuidStrings);
```

### String ID (StringBaseRepository)
```java
// Find by pattern (case-insensitive)
List<Country> countries = countryRepository.findByIdLikeIgnoreCase("%US%");

// Find by prefix
List<Country> countries = countryRepository.findByIdStartingWith("US");

// Find by suffix
List<Country> countries = countryRepository.findByIdEndingWith("A");

// Find by ID length
List<Country> countries = countryRepository.findByIdLength(2); // ISO codes
```

## 💡 Quick Examples

### Complete CRUD Example
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    // Create
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    // Read (active only)
    public List<User> getAllActiveUsers() {
        return userRepository.findAllActive();
    }
    
    // Read by ID (active only)
    public Optional<User> getUser(Long id) {
        return userRepository.findByIdNotDeleted(id);
    }
    
    // Update (automatic auditing)
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    // Soft Delete
    public void deleteUser(Long id) {
        userRepository.softDeleteById(id);
    }
    
    // Restore
    public void restoreUser(Long id) {
        userRepository.restoreById(id);
    }
}
```

### Bulk Operations Example
```java
@Service
public class AdminService {
    
    // Bulk soft delete
    public void deactivateUsers(List<Long> userIds) {
        int deletedCount = userRepository.softDeleteByIds(userIds);
        log.info("Soft deleted {} users", deletedCount);
    }
    
    // Cleanup old records
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldRecords() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        int cleaned = userRepository.permanentlyDeleteOldRecords(sixMonthsAgo);
        log.info("Permanently deleted {} old records", cleaned);
    }
    
    // Get statistics
    public Map<String, Long> getStatistics() {
        return Map.of(
            "active", userRepository.countActive(),
            "deleted", userRepository.countDeleted(),
            "total", userRepository.countTotal()
        );
    }
}
```

### Date Range Queries Example
```java
@Service
public class ReportService {
    
    public List<User> getNewUsersThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findByCreatedAtBetween(startOfMonth, now);
    }
    
    public List<User> getRecentlyActiveUsers() {
        return userRepository.findRecentlyUpdated(); // Last 7 days
    }
    
    public List<User> getTodaysSignups() {
        return userRepository.findCreatedToday();
    }
}
```

## 🚀 Common Patterns

### Pattern 1: Safe Delete with Confirmation
```java
public boolean safeDeleteUser(Long userId) {
    if (userRepository.existsByIdNotDeleted(userId)) {
        userRepository.softDeleteById(userId);
        return true;
    }
    return false;
}
```

### Pattern 2: Batch Processing with Status Check
```java
public void processPendingUsers() {
    List<User> activeUsers = userRepository.findAllActive();
    // Process only active users
    activeUsers.forEach(this::processUser);
}
```

### Pattern 3: Audit Trail
```java
public List<User> getUsersByAdmin(String adminName) {
    return userRepository.findByCreatedBy(adminName);
}
```

### Pattern 4: Data Archival
```java
@Scheduled(fixedRate = 86400000) // Daily
public void archiveOldData() {
    LocalDateTime cutoff = LocalDateTime.now().minusYears(1);
    
    // Get records to archive
    List<User> oldRecords = userRepository.findByCreatedAtBetween(
        LocalDateTime.MIN, cutoff
    );
    
    // Archive them (your logic)
    archiveService.archive(oldRecords);
    
    // Permanently delete
    userRepository.permanentlyDeleteOldRecords(cutoff);
}
```

## 📝 Method Return Types

| Method Type | Return Type | Description |
|-------------|-------------|-------------|
| `find*` | `List<T>`, `Optional<T>`, `Page<T>` | Query results |
| `softDelete*` | `int` | Number of affected records |
| `restore*` | `int` | Number of affected records |
| `count*` | `long` | Count of records |
| `exists*` | `boolean` | True if exists |
| `is*` | `boolean` | True/false status |

## ⚡ Performance Tips

1. **Use pagination for large datasets:**
   ```java
   Page<User> users = userRepository.findAllActive(PageRequest.of(0, 20));
   ```

2. **Batch operations are more efficient:**
   ```java
   // Better than multiple single deletes
   userRepository.softDeleteByIds(userIds);
   ```

3. **Filter at database level:**
   ```java
   // Good - filters in database
   List<User> users = userRepository.findAllActive();
   
   // Bad - filters in memory
   List<User> users = userRepository.findAll().stream()
       .filter(u -> !u.isDeleted())
       .collect(toList());
   ```

---

**💡 Remember:** All `find*` methods automatically exclude soft-deleted records unless specifically querying for deleted records!
