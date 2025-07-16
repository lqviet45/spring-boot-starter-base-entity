package com.lqviet.baseentity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ============ COMMON BASE REPOSITORY (Reduces Duplication) ============

/**
 * Base repository interface containing all common CRUD operations and soft delete functionality.
 * This interface works with any ID type through generics.
 *
 * @param <T> the entity type that extends AbstractBaseEntity
 * @param <ID> the type of the primary key
 *
 * @author Le Quoc Viet
 * @version 1.1.0
 */
@NoRepositoryBean
interface CommonBaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    // ============ FIND OPERATIONS ============

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = false")
    Optional<T> findByIdNotDeleted(@Param("id") ID id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false ORDER BY e.createdAt DESC")
    List<T> findAllActive();

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false")
    Page<T> findAllActive(Pageable pageable);

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = true ORDER BY e.updatedAt DESC")
    List<T> findAllDeleted();

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = true")
    Page<T> findAllDeleted(Pageable pageable);

    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt BETWEEN :startDate AND :endDate AND e.isDeleted = false ORDER BY e.createdAt")
    List<T> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt BETWEEN :startDate AND :endDate AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<T> findByUpdatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM #{#entityName} e WHERE e.createdBy = :createdBy AND e.isDeleted = false ORDER BY e.createdAt DESC")
    List<T> findByCreatedBy(@Param("createdBy") String createdBy);

    @Query("SELECT e FROM #{#entityName} e WHERE e.lastModifiedBy = :lastModifiedBy AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<T> findByLastModifiedBy(@Param("lastModifiedBy") String lastModifiedBy);

    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt >= :cutoffDate AND e.isDeleted = false ORDER BY e.createdAt DESC")
    List<T> findRecentlyCreated(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt >= :cutoffDate AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<T> findRecentlyUpdated(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids AND e.isDeleted = false")
    List<T> findByIdIn(@Param("ids") List<ID> ids);

    // ============ SOFT DELETE OPERATIONS ============

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = true, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.isDeleted = false")
    int softDeleteById(@Param("id") ID id);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = false, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.isDeleted = true")
    int restoreById(@Param("id") ID id);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = true, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id IN :ids AND e.isDeleted = false")
    int softDeleteByIds(@Param("ids") List<ID> ids);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = false, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id IN :ids AND e.isDeleted = true")
    int restoreByIds(@Param("ids") List<ID> ids);

    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.isDeleted = true AND e.updatedAt < :cutoffDate")
    int permanentlyDeleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ============ COUNT OPERATIONS ============

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.isDeleted = false")
    long countActive();

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.isDeleted = true")
    long countDeleted();

    @Query("SELECT COUNT(e) FROM #{#entityName} e")
    long countTotal();

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = false")
    boolean existsByIdNotDeleted(@Param("id") ID id);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.createdBy = :createdBy AND e.isDeleted = false")
    boolean existsByCreatedBy(@Param("createdBy") String createdBy);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = true")
    boolean isSoftDeleted(@Param("id") ID id);

    // ============ CONVENIENCE METHODS ============

    default List<T> findRecentlyCreated() {
        return findRecentlyCreated(LocalDateTime.now().minusDays(7));
    }

    default List<T> findRecentlyUpdated() {
        return findRecentlyUpdated(LocalDateTime.now().minusDays(7));
    }

    default List<T> findCreatedToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        return findByCreatedAtBetween(startOfDay, endOfDay);
    }

    default List<T> findUpdatedToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        return findByUpdatedAtBetween(startOfDay, endOfDay);
    }
}