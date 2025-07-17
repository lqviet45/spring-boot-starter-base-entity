# 📖 Repository Methods Complete Guide

A comprehensive reference for all built-in repository methods provided by the Spring Boot Starter Base Entity library.

## 📋 Table of Contents

- [🔍 Find Operations](#-find-operations)
- [🗑️ Soft Delete Operations](#️-soft-delete-operations)
- [📊 Count & Statistics](#-count--statistics)
- [🆔 ID Type-Specific Methods](#-id-type-specific-methods)
- [💡 Usage Examples](#-usage-examples)
- [⚡ Performance Tips](#-performance-tips)
- [🎯 Common Patterns](#-common-patterns)

---

## 🔍 Find Operations

### **Basic Find Methods**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `findAllActive()` | `List<T>` | All non-deleted records | `userRepo.findAllActive()` |
| `findAllActive(Pageable)` | `Page<T>` | Paginated non-deleted records | `userRepo.findAllActive(PageRequest.of(0, 20))` |
| `findByIdNotDeleted(ID)` | `Optional<T>` | Find by ID (active only) | `userRepo.findByIdNotDeleted(1L)` |
| `findAllDeleted()` | `List<T>` | All soft-deleted records | `userRepo.findAllDeleted()` |
| `findAllDeleted(Pageable)` | `Page<T>` | Paginated soft-deleted records | `userRepo.findAllDeleted(pageable)` |
| `findByIdIn(List<ID>)` | `List<T>` | Find by multiple IDs (active only) | `userRepo.findByIdIn(Arrays.asList(1L, 2L, 3L))` |

```java
// Basic find operations
List<User> activeUsers = userRepository.findAllActive();
Optional<User> user = userRepository.findByIdNotDeleted(1L);
Page<User> userPage = userRepository.findAllActive(PageRequest.of(0, 20));

// Find deleted records for admin purposes
List<User> deletedUsers = userRepository.findAllDeleted();
```

### **Date-Based Query Methods**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `findByCreatedAtBetween(start, end)` | `List<T>` | Created within date range | `userRepo.findByCreatedAtBetween(start, end)` |
| `findByUpdatedAtBetween(start, end)` | `List<T>` | Updated within date range | `userRepo.findByUpdatedAtBetween(start, end)` |
| `findRecentlyCreated()` | `List<T>` | Created in last 7 days | `userRepo.findRecentlyCreated()` |
| `findRecentlyCreated(cutoff)` | `List<T>` | Created after cutoff date | `userRepo.findRecentlyCreated(LocalDateTime.now().minusDays(30))` |
| `findRecentlyUpdated()` | `List<T>` | Updated in last 7 days | `userRepo.findRecentlyUpdated()` |
| `findRecentlyUpdated(cutoff)` | `List<T>` | Updated after cutoff date | `userRepo.findRecentlyUpdated(LocalDateTime.now().minusHours(1))` |
| `findCreatedToday()` | `List<T>` | Created today | `userRepo.findCreatedToday()` |
| `findUpdatedToday()` | `List<T>` | Updated today | `userRepo.findUpdatedToday()` |

```java
// Date range queries
LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
LocalDateTime now = LocalDateTime.now();
List<User> monthlyUsers = userRepository.findByCreatedAtBetween(startOfMonth, now);

// Quick date shortcuts
List<User> recentUsers = userRepository.findRecentlyCreated(); // Last 7 days
List<User> todayUsers = userRepository.findCreatedToday();
List<User> activeToday = userRepository.findUpdatedToday();

// Custom time periods
LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
List<User> veryRecentUsers = userRepository.findRecentlyUpdated(lastHour);
```

### **Audit-Based Query Methods**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `findByCreatedBy(creator)` | `List<T>` | Find by creator username | `userRepo.findByCreatedBy("admin")` |
| `findByLastModifiedBy(modifier)` | `List<T>` | Find by last modifier | `userRepo.findByLastModifiedBy("system")` |

```java
// Audit trail queries
List<User> adminCreatedUsers = userRepository.findByCreatedBy("admin");
List<User> systemModified = userRepository.findByLastModifiedBy("system");

// Track specific user's work
List<User> johnsWork = userRepository.findByCreatedBy("john.doe");
```

---

## 🗑️ Soft Delete Operations

### **Single Record Operations**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `softDeleteById(ID)` | `int` | Soft delete by ID | `userRepo.softDeleteById(1L)` |
| `restoreById(ID)` | `int` | Restore soft-deleted record | `userRepo.restoreById(1L)` |
| `softDelete(entity)` | `boolean` | Soft delete entity directly | `userRepo.softDelete(user)` |
| `restore(entity)` | `boolean` | Restore entity directly | `userRepo.restore(user)` |

```java
// Single record soft delete
int deleted = userRepository.softDeleteById(1L);
if (deleted > 0) {
    log.info("User successfully soft deleted");
}

// Entity-based operations
User user = userRepository.findByIdNotDeleted(1L).orElseThrow();
boolean success = userRepository.softDelete(user);

// Restore operations
int restored = userRepository.restoreById(1L);
boolean entityRestored = userRepository.restore(user);
```

### **Bulk Operations**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `softDeleteByIds(List<ID>)` | `int` | Bulk soft delete by IDs | `userRepo.softDeleteByIds(Arrays.asList(1L, 2L, 3L))` |
| `restoreByIds(List<ID>)` | `int` | Bulk restore by IDs | `userRepo.restoreByIds(Arrays.asList(1L, 2L))` |
| `permanentlyDeleteOldRecords(cutoff)` | `int` | Permanently delete old soft-deleted records | `userRepo.permanentlyDeleteOldRecords(sixMonthsAgo)` |

```java
// Bulk soft delete
List<Long> userIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
int deletedCount = userRepository.softDeleteByIds(userIds);
log.info("Soft deleted {} users", deletedCount);

// Bulk restore
List<Long> restoreIds = Arrays.asList(1L, 2L);
int restoredCount = userRepository.restoreByIds(restoreIds);
log.info("Restored {} users", restoredCount);

// Cleanup old deleted records (run periodically)
LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
int cleaned = userRepository.permanentlyDeleteOldRecords(sixMonthsAgo);
log.info("Permanently deleted {} old records", cleaned);
```

---

## 📊 Count & Statistics

### **Count Methods**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `countActive()` | `long` | Count non-deleted records | `userRepo.countActive()` |
| `countDeleted()` | `long` | Count soft-deleted records | `userRepo.countDeleted()` |
| `countTotal()` | `long` | Count all records (active + deleted) | `userRepo.countTotal()` |

```java
// Get comprehensive statistics
long activeUsers = userRepository.countActive();
long deletedUsers = userRepository.countDeleted();
long totalUsers = userRepository.countTotal();

log.info("Users: {} active, {} deleted, {} total", activeUsers, deletedUsers, totalUsers);

// Calculate percentages
double deletionRate = (double) deletedUsers / totalUsers * 100;
log.info("Deletion rate: {:.2f}%", deletionRate);
```

### **Existence Check Methods**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `existsByIdNotDeleted(ID)` | `boolean` | Check if active record exists | `userRepo.existsByIdNotDeleted(1L)` |
| `existsByCreatedBy(creator)` | `boolean` | Check if records exist by creator | `userRepo.existsByCreatedBy("admin")` |
| `isSoftDeleted(ID)` | `boolean` | Check if record is soft-deleted | `userRepo.isSoftDeleted(1L)` |

```java
// Safe existence checks
if (userRepository.existsByIdNotDeleted(userId)) {
    // User exists and is active
    processUser(userId);
} else {
    log.warn("User {} not found or is deleted", userId);
}

// Check deletion status
if (userRepository.isSoftDeleted(userId)) {
    log.info("User {} is soft deleted, offering restore option", userId);
}

// Audit checks
boolean hasAdminCreatedUsers = userRepository.existsByCreatedBy("admin");
if (hasAdminCreatedUsers) {
    log.info("Found users created by admin");
}
```

---

## 🆔 ID Type-Specific Methods

### **Long ID Methods (BaseRepository)**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `findByIdRange(start, end)` | `List<T>` | Find by ID range | `userRepo.findByIdRange(1L, 100L)` |

```java
// ID range queries for Long-based entities
List<User> earlyUsers = userRepository.findByIdRange(1L, 1000L);
List<User> recentUsers = userRepository.findByIdRange(10000L, 20000L);

// Useful for batch processing
List<User> batchUsers = userRepository.findByIdRange(startId, endId);
processBatch(batchUsers);
```

### **UUID Methods (UuidBaseRepository)**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `findByIdStringNotDeleted(String)` | `Optional<T>` | Find by UUID string | `orderRepo.findByIdStringNotDeleted("550e8400-...")` |
| `findByIdStringsNotDeleted(List<String>)` | `List<T>` | Find by multiple UUID strings | `orderRepo.findByIdStringsNotDeleted(uuidStrings)` |

```java
// UUID string operations
String uuidString = "550e8400-e29b-41d4-a716-446655440000";
Optional<Order> order = orderRepository.findByIdStringNotDeleted(uuidString);

// Multiple UUID strings
List<String> orderUuids = Arrays.asList(
    "550e8400-e29b-41d4-a716-446655440000",
    "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
);
List<Order> orders = orderRepository.findByIdStringsNotDeleted(orderUuids);

// Convert from UUIDs to strings
List<UUID> uuidList = getOrderUuids();
List<String> uuidStrings = uuidList.stream()
    .map(UUID::toString)
    .collect(toList());
List<Order> foundOrders = orderRepository.findByIdStringsNotDeleted(uuidStrings);
```

### **String ID Methods (StringBaseRepository)**

| Method | Return Type | Description | Example |
|--------|-------------|-------------|---------|
| `findByIdLikeIgnoreCase(pattern)` | `List<T>` | Pattern matching (case-insensitive) | `countryRepo.findByIdLikeIgnoreCase("%US%")` |
| `findByIdStartingWith(prefix)` | `List<T>` | Find by prefix | `countryRepo.findByIdStartingWith("US")` |
| `findByIdEndingWith(suffix)` | `List<T>` | Find by suffix | `countryRepo.findByIdEndingWith("A")` |
| `findByIdLength(length)` | `List<T>` | Find by ID length | `countryRepo.findByIdLength(2)` |

```java
// Pattern matching for String IDs
List<Country> usCountries = countryRepository.findByIdLikeIgnoreCase("%US%");
List<Country> americanCountries = countryRepository.findByIdEndingWith("A");

// Prefix/suffix searches
List<Product> usCodes = productRepository.findByIdStartingWith("US");
List<Product> typeBProducts = productRepository.findByIdEndingWith("-B");

// Length-based searches
List<Country> isoCodes = countryRepository.findByIdLength(2); // ISO 2-letter codes
List<Country> iso3Codes = countryRepository.findByIdLength(3); // ISO 3-letter codes

// Complex pattern example
List<Product> pattern = productRepository.findByIdLikeIgnoreCase("PROD-%-2024");
```

---

## 💡 Usage Examples

### **Complete CRUD Service Implementation**

```java
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    // CREATE - with automatic auditing
    public User createUser(CreateUserRequest request) {
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .build();
        
        return userRepository.save(user);
        // ✨ Automatically sets: createdAt, updatedAt, createdBy, version
    }
    
    // READ - safe operations that exclude deleted records
    public Optional<User> getUser(Long id) {
        return userRepository.findByIdNotDeleted(id);
    }
    
    public List<User> getAllActiveUsers() {
        return userRepository.findAllActive();
    }
    
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAllActive(pageable);
    }
    
    // UPDATE - with automatic auditing
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdNotDeleted(id)
            .orElseThrow(() -> new UserNotFoundException(id));
            
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        
        return userRepository.save(user);
        // ✨ Automatically updates: updatedAt, lastModifiedBy, version
    }
    
    // SOFT DELETE - preserves data
    public void deleteUser(Long id) {
        if (!userRepository.existsByIdNotDeleted(id)) {
            throw new UserNotFoundException(id);
        }
        
        int deleted = userRepository.softDeleteById(id);
        if (deleted == 0) {
            throw new UserDeletionException(id);
        }
    }
    
    // RESTORE - recover soft-deleted users
    public void restoreUser(Long id) {
        if (!userRepository.isSoftDeleted(id)) {
            throw new UserNotDeletedException(id);
        }
        
        int restored = userRepository.restoreById(id);
        if (restored == 0) {
            throw new UserRestoreException(id);
        }
    }
}
```

### **Analytics and Reporting Service**

```java
@Service
public class UserAnalyticsService {
    
    private final UserRepository userRepository;
    
    @Cacheable("user-stats")
    public UserStatistics getUserStatistics() {
        return UserStatistics.builder()
            .totalActive(userRepository.countActive())
            .totalDeleted(userRepository.countDeleted())
            .totalUsers(userRepository.countTotal())
            .newToday(userRepository.findCreatedToday().size())
            .updatedToday(userRepository.findUpdatedToday().size())
            .newThisWeek(userRepository.findRecentlyCreated().size())
            .build();
    }
    
    public List<User> getNewUsersInPeriod(LocalDateTime start, LocalDateTime end) {
        return userRepository.findByCreatedAtBetween(start, end);
    }
    
    public List<User> getActiveUsersInPeriod(LocalDateTime start, LocalDateTime end) {
        return userRepository.findByUpdatedAtBetween(start, end);
    }
    
    public UserGrowthReport getGrowthReport() {
        LocalDateTime now = LocalDateTime.now();
        
        return UserGrowthReport.builder()
            .todaySignups(userRepository.findCreatedToday().size())
            .weeklySignups(userRepository.findRecentlyCreated().size())
            .monthlySignups(getNewUsersInPeriod(
                now.withDayOfMonth(1).toLocalDate().atStartOfDay(), now).size())
            .build();
    }
    
    public List<UserActivity> getCreatorActivity() {
        return userRepository.findAllActive().stream()
            .collect(groupingBy(User::getCreatedBy, counting()))
            .entrySet().stream()
            .map(entry -> new UserActivity(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
            .collect(toList());
    }
}
```

### **Admin Management Service**

```java
@Service
public class AdminUserService {
    
    private final UserRepository userRepository;
    
    // Bulk operations for admin interface
    public BulkOperationResult bulkDeleteUsers(List<Long> userIds) {
        // Validate all users exist and are active
        List<Long> validIds = userIds.stream()
            .filter(userRepository::existsByIdNotDeleted)
            .collect(toList());
            
        if (validIds.isEmpty()) {
            return BulkOperationResult.noValidIds();
        }
        
        int deletedCount = userRepository.softDeleteByIds(validIds);
        
        return BulkOperationResult.builder()
            .successful(deletedCount)
            .failed(userIds.size() - deletedCount)
            .invalidIds(userIds.stream()
                .filter(id -> !validIds.contains(id))
                .collect(toList()))
            .build();
    }
    
    public BulkOperationResult bulkRestoreUsers(List<Long> userIds) {
        // Find deleted users
        List<Long> deletedIds = userIds.stream()
            .filter(userRepository::isSoftDeleted)
            .collect(toList());
            
        int restoredCount = userRepository.restoreByIds(deletedIds);
        
        return BulkOperationResult.builder()
            .successful(restoredCount)
            .failed(deletedIds.size() - restoredCount)
            .notDeletedIds(userIds.stream()
                .filter(id -> !userRepository.isSoftDeleted(id))
                .collect(toList()))
            .build();
    }
    
    // Data cleanup operations
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void performDailyCleanup() {
        // Remove records deleted more than 6 months ago
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
        int cleaned = userRepository.permanentlyDeleteOldRecords(cutoff);
        
        if (cleaned > 0) {
            log.info("Daily cleanup: permanently deleted {} records older than {}", 
                cleaned, cutoff);
        }
    }
    
    // Admin dashboard data
    public AdminDashboard getDashboardData() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        
        return AdminDashboard.builder()
            .totalUsers(userRepository.countTotal())
            .activeUsers(userRepository.countActive())
            .deletedUsers(userRepository.countDeleted())
            .todaySignups(userRepository.findByCreatedAtBetween(startOfDay, LocalDateTime.now()).size())
            .recentActivity(userRepository.findRecentlyUpdated())
            .deletedToday(userRepository.findAllDeleted().stream()
                .filter(user -> user.getUpdatedAt().isAfter(startOfDay))
                .collect(toList()))
            .build();
    }
}
```

### **Data Migration and Import Service**

```java
@Service
@Transactional
public class UserMigrationService {
    
    private final UserRepository userRepository;
    
    public ImportResult importUsers(List<UserImportData> importData) {
        List<User> savedUsers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (UserImportData data : importData) {
            try {
                // Check if user already exists
                if (userRepository.existsByIdNotDeleted(data.getId())) {
                    errors.add("User " + data.getId() + " already exists");
                    continue;
                }
                
                User user = convertToUser(data);
                User saved = userRepository.save(user);
                savedUsers.add(saved);
                
            } catch (Exception e) {
                errors.add("Error importing user " + data.getId() + ": " + e.getMessage());
            }
        }
        
        return ImportResult.builder()
            .totalProcessed(importData.size())
            .successful(savedUsers.size())
            .failed(errors.size())
            .errors(errors)
            .importedUsers(savedUsers)
            .build();
    }
    
    public void migrateDeletedUsers() {
        // Find users deleted more than 30 days ago
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<User> oldDeletedUsers = userRepository.findAllDeleted().stream()
            .filter(user -> user.getUpdatedAt().isBefore(cutoff))
            .collect(toList());
            
        // Archive to external system
        archiveUsers(oldDeletedUsers);
        
        // Permanently delete from main database
        List<Long> idsToDelete = oldDeletedUsers.stream()
            .map(User::getId)
            .collect(toList());
            
        oldDeletedUsers.forEach(userRepository::delete); // Hard delete
        
        log.info("Migrated and deleted {} old users", idsToDelete.size());
    }
}
```

---

## ⚡ Performance Tips

### **1. Use Pagination for Large Datasets**

```java
// ✅ Good - Paginated queries
@GetMapping("/users")
public Page<User> getUsers(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return userRepository.findAllActive(pageable);
}

// ❌ Bad - Loading all records
@GetMapping("/users")  
public List<User> getAllUsers() {
    return userRepository.findAllActive(); // Could load millions of records
}
```

### **2. Batch Operations Are More Efficient**

```java
// ✅ Good - Single batch operation
List<Long> userIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
int deletedCount = userRepository.softDeleteByIds(userIds);

// ❌ Bad - Multiple individual operations
List<Long> userIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
userIds.forEach(id -> userRepository.softDeleteById(id)); // N queries
```

### **3. Filter at Database Level, Not in Memory**

```java
// ✅ Good - Database filtering
List<User> activeUsers = userRepository.findAllActive();

// ✅ Good - Database date filtering  
List<User> recentUsers = userRepository.findRecentlyCreated();

// ❌ Bad - Memory filtering
List<User> users = userRepository.findAll().stream()
    .filter(u -> !u.isDeleted())
    .filter(u -> u.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)))
    .collect(toList());
```

### **4. Use Existence Checks Instead of Loading Entities**

```java
// ✅ Good - Existence check
if (userRepository.existsByIdNotDeleted(userId)) {
    // User exists and is active
    processUser(userId);
}

// ❌ Bad - Loading full entity just to check existence
Optional<User> user = userRepository.findByIdNotDeleted(userId);
if (user.isPresent()) {
    processUser(userId);
}
```

### **5. Use Appropriate ID Types for Your Use Case**

```java
// ✅ Good for single database - Auto-incrementing Long
@Entity
public class User extends BaseEntity { } // ID: 1, 2, 3, 4...

// ✅ Good for distributed systems - UUIDv7  
@Entity
public class Order extends UuidBaseEntity {
    public Order() {
        setId(UuidV7Utils.generate()); // Time-ordered, better performance
    }
}

// ✅ Good for natural keys - String
@Entity  
public class Country extends StringBaseEntity {
    public Country(String code) {
        setId(code); // "US", "CA", "GB"
    }
}
```

---

## 🎯 Common Patterns

### **Pattern 1: Safe Entity Operations with Validation**

```java
@Service
public class SafeUserService {
    
    public User getActiveUser(Long id) {
        return userRepository.findByIdNotDeleted(id)
            .orElseThrow(() -> new UserNotFoundException("User not found or deleted: " + id));
    }
    
    public void safeDeleteUser(Long id) {
        if (!userRepository.existsByIdNotDeleted(id)) {
            throw new UserNotFoundException("Cannot delete: user not found or already deleted");
        }
        
        int deleted = userRepository.softDeleteById(id);
        if (deleted == 0) {
            throw new UserOperationException("Failed to delete user: " + id);
        }
    }
    
    public void safeRestoreUser(Long id) {
        if (!userRepository.isSoftDeleted(id)) {
            throw new UserOperationException("Cannot restore: user is not deleted");
        }
        
        int restored = userRepository.restoreById(id);
        if (restored == 0) {
            throw new UserOperationException("Failed to restore user: " + id);
        }
    }
}
```

### **Pattern 2: Audit Trail and Activity Tracking**

```java
@Service
public class UserAuditService {
    
    public List<User> getUsersCreatedBy(String creator) {
        return userRepository.findByCreatedBy(creator);
    }
    
    public List<User> getUsersModifiedBy(String modifier) {
        return userRepository.findByLastModifiedBy(modifier);
    }
    
    public AuditReport generateAuditReport(LocalDateTime start, LocalDateTime end) {
        List<User> createdInPeriod = userRepository.findByCreatedAtBetween(start, end);
        List<User> modifiedInPeriod = userRepository.findByUpdatedAtBetween(start, end);
        
        return AuditReport.builder()
            .period(start + " to " + end)
            .usersCreated(createdInPeriod.size())
            .usersModified(modifiedInPeriod.size())
            .creatorStats(createdInPeriod.stream()
                .collect(groupingBy(User::getCreatedBy, counting())))
            .modifierStats(modifiedInPeriod.stream()
                .collect(groupingBy(User::getLastModifiedBy, counting())))
            .build();
    }
}
```

### **Pattern 3: Batch Processing with Error Handling**

```java
@Service
public class UserBatchService {
    
    @Transactional
    public BatchResult processBatch(List<Long> userIds, Function<User, User> processor) {
        List<User> processed = new ArrayList<>();
        List<BatchError> errors = new ArrayList<>();
        
        for (Long id : userIds) {
            try {
                Optional<User> userOpt = userRepository.findByIdNotDeleted(id);
                if (userOpt.isEmpty()) {
                    errors.add(new BatchError(id, "User not found or deleted"));
                    continue;
                }
                
                User user = userOpt.get();
                User processedUser = processor.apply(user);
                User saved = userRepository.save(processedUser);
                processed.add(saved);
                
            } catch (Exception e) {
                errors.add(new BatchError(id, e.getMessage()));
            }
        }
        
        return BatchResult.builder()
            .totalProcessed(userIds.size())
            .successful(processed.size())
            .failed(errors.size())
            .processedUsers(processed)
            .errors(errors)
            .build();
    }
}
```

### **Pattern 4: Data Archival and Cleanup**

```java
@Service
public class UserArchivalService {
    
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void performDailyArchival() {
        // Archive users deleted more than 90 days ago
        LocalDateTime archivalCutoff = LocalDateTime.now().minusDays(90);
        
        List<User> usersToArchive = userRepository.findAllDeleted().stream()
            .filter(user -> user.getUpdatedAt().isBefore(archivalCutoff))
            .collect(toList());
            
        if (!usersToArchive.isEmpty()) {
            // Archive to external storage
            archiveToExternalStorage(usersToArchive);
            
            // Permanently delete from main database
            usersToArchive.forEach(userRepository::delete);
            
            log.info("Archived and deleted {} users older than {}", 
                usersToArchive.size(), archivalCutoff);
        }
    }
    
    public ArchivalReport generateArchivalReport() {
        LocalDateTime cutoff90Days = LocalDateTime.now().minusDays(90);
        LocalDateTime cutoff30Days = LocalDateTime.now().minusDays(30);
        
        List<User> deletedUsers = userRepository.findAllDeleted();
        
        long eligibleForArchival = deletedUsers.stream()
            .filter(user -> user.getUpdatedAt().isBefore(cutoff90Days))
            .count();
            
        long recentlyDeleted = deletedUsers.stream()
            .filter(user -> user.getUpdatedAt().isAfter(cutoff30Days))
            .count();
            
        return ArchivalReport.builder()
            .totalDeleted(deletedUsers.size())
            .eligibleForArchival(eligibleForArchival)
            .recentlyDeleted(recentlyDeleted)
            .archivalCutoffDate(cutoff90Days)
            .build();
    }
}
```

### **Pattern 5: Advanced Filtering with SoftDeleteUtils**

```java
@Service
public class UserFilterService {
    
    public List<User> getFilteredUsers(UserFilter filter) {
        // Start with all users (including deleted for admin)
        List<User> allUsers = userRepository.findAll();
        
        // Apply soft delete filter
        List<User> users = filter.includeDeleted() ? 
            allUsers : SoftDeleteUtils.filterActive(allUsers);
        
        // Apply additional business filters
        return users.stream()
            .filter(user -> matchesFilter(user, filter))
            .collect(toList());
    }
    
    public void bulkProcessActiveUsers(List<User> users, UserProcessor processor) {
        List<User> activeUsers = SoftDeleteUtils.filterActive(users);
        
        for (User user : activeUsers) {
            if (SoftDeleteUtils.canDelete(user)) {
                processor.process(user);
            }
        }
    }
    
    public CleanupResult cleanupOldUsers() {
        List<User> allUsers = userRepository.findAll();
        LocalDateTime cutoff = LocalDateTime.now().minusYears(2);
        
        // Find deleted users eligible for permanent deletion
        List<User> eligibleForDeletion = SoftDeleteUtils
            .getEligibleForPermanentDeletion(allUsers, cutoff);
            
        // Archive before permanent deletion
        archiveUsers(eligibleForDeletion);
        
        // Permanently delete
        int deletedCount = 0;
        for (User user : eligibleForDeletion) {
            userRepository.delete(user); // Hard delete
            deletedCount++;
        }
        
        return CleanupResult.builder()
            .eligibleUsers(eligibleForDeletion.size())
            .archivedUsers(eligibleForDeletion.size())
            .deletedUsers(deletedCount)
            .cutoffDate(cutoff)
            .build();
    }
}
```

### **Pattern 6: Statistics and Analytics**

```java
@Service
public class UserStatisticsService {
    
    @Cacheable(value = "user-stats", key = "#period")
    public UserStatistics getStatistics(StatisticsPeriod period) {
        LocalDateTime[] range = calculateDateRange(period);
        LocalDateTime start = range[0];
        LocalDateTime end = range[1];
        
        return UserStatistics.builder()
            // Current counts
            .activeUsers(userRepository.countActive())
            .deletedUsers(userRepository.countDeleted())
            .totalUsers(userRepository.countTotal())
            
            // Period-specific data
            .newUsersInPeriod(userRepository.findByCreatedAtBetween(start, end).size())
            .modifiedUsersInPeriod(userRepository.findByUpdatedAtBetween(start, end).size())
            
            // Quick stats
            .newToday(userRepository.findCreatedToday().size())
            .activeToday(userRepository.findUpdatedToday().size())
            .newThisWeek(userRepository.findRecentlyCreated().size())
            
            // Growth metrics
            .growthRate(calculateGrowthRate(period))
            .churnRate(calculateChurnRate(period))
            
            .generatedAt(LocalDateTime.now())
            .period(period.name())
            .build();
    }
    
    public List<CreatorActivity> getCreatorStatistics() {
        return userRepository.findAllActive().stream()
            .collect(groupingBy(
                User::getCreatedBy,
                collectingAndThen(counting(), count -> count)
            ))
            .entrySet().stream()
            .map(entry -> CreatorActivity.builder()
                .creatorName(entry.getKey())
                .usersCreated(entry.getValue())
                .lastActivity(getLastActivityForCreator(entry.getKey()))
                .build())
            .sorted((a, b) -> Long.compare(b.getUsersCreated(), a.getUsersCreated()))
            .collect(toList());
    }
    
    private LocalDateTime getLastActivityForCreator(String creator) {
        return userRepository.findByCreatedBy(creator).stream()
            .map(User::getCreatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }
}
```

---

## 📚 Method Return Types Reference

| Return Type | Description | When to Use |
|-------------|-------------|-------------|
| `List<T>` | Collection of entities | When you expect multiple results |
| `Optional<T>` | Single entity that may not exist | Single entity lookups |
| `Page<T>` | Paginated results with metadata | Large datasets with pagination |
| `int` | Number of affected records | Bulk operations (delete, restore) |
| `long` | Count of records | Statistics and counting operations |
| `boolean` | True/false result | Existence checks, validation |

## 🔄 Operation Status Codes

When using bulk operations, here's what the return values mean:

```java
// Soft delete operations
int result = userRepository.softDeleteById(1L);
// result > 0: Successfully deleted
// result = 0: No record found or already deleted

// Restore operations  
int result = userRepository.restoreById(1L);
// result > 0: Successfully restored
// result = 0: No deleted record found

// Bulk operations
int result = userRepository.softDeleteByIds(Arrays.asList(1L, 2L, 3L));
// result = number of actually deleted records
// May be less than input list size if some IDs don't exist
```

## 🚨 Common Pitfalls and Solutions

### **Pitfall 1: Forgetting About Soft Delete**

```java
// ❌ Problem: Using standard JPA methods
List<User> users = userRepository.findAll(); // Includes deleted records!

// ✅ Solution: Use BaseRepository methods
List<User> users = userRepository.findAllActive(); // Only active records
```

### **Pitfall 2: Not Handling Optional Results**

```java
// ❌ Problem: Assuming entity exists
User user = userRepository.findByIdNotDeleted(1L).get(); // May throw exception!

// ✅ Solution: Proper Optional handling
User user = userRepository.findByIdNotDeleted(1L)
    .orElseThrow(() -> new UserNotFoundException("User not found: " + 1L));
```

### **Pitfall 3: Inefficient Existence Checks**

```java
// ❌ Problem: Loading entity just to check existence
boolean exists = userRepository.findByIdNotDeleted(1L).isPresent();

// ✅ Solution: Use existence methods
boolean exists = userRepository.existsByIdNotDeleted(1L);
```

### **Pitfall 4: Memory vs Database Filtering**

```java
// ❌ Problem: Filtering in memory
List<User> recentUsers = userRepository.findAllActive().stream()
    .filter(u -> u.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)))
    .collect(toList());

// ✅ Solution: Database filtering
List<User> recentUsers = userRepository.findRecentlyCreated();
```

## 🎯 Quick Reference Card

### **Most Used Methods**
```java
// Essential CRUD
userRepository.findAllActive()                    // Get all active
userRepository.findByIdNotDeleted(id)            // Get by ID safely
userRepository.save(user)                        // Create/Update
userRepository.softDeleteById(id)                // Soft delete

// Statistics
userRepository.countActive()                     // Count active
userRepository.existsByIdNotDeleted(id)          // Check existence

// Time-based
userRepository.findRecentlyCreated()             // Last 7 days
userRepository.findCreatedToday()                // Today only

// Bulk operations
userRepository.softDeleteByIds(ids)              // Bulk delete
userRepository.restoreByIds(ids)                 // Bulk restore
```

### **Emergency Recovery**
```java
// Restore accidentally deleted user
userRepository.restoreById(userId);

// Find all deleted users
List<User> deleted = userRepository.findAllDeleted();

// Check if user is deleted
boolean isDeleted = userRepository.isSoftDeleted(userId);
```

---

## 📞 Need Help?

- **📚 Library Documentation**: [Main README](README.md)
- **🐛 Report Issues**: [GitHub Issues](https://github.com/lqviet45/spring-boot-starter-base-entity/issues)
- **💡 Feature Requests**: [GitHub Discussions](https://github.com/lqviet45/spring-boot-starter-base-entity/discussions)

---

<div align="center">

**⭐ Found this guide helpful? Star the repository!**

[🏠 Back to Main README](README.md) • [🐛 Report Issue](https://github.com/lqviet45/spring-boot-starter-base-entity/issues) • [💡 Suggest Feature](https://github.com/lqviet45/spring-boot-starter-base-entity/issues/new)

</div>
