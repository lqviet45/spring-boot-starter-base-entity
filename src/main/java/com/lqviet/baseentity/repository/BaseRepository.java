package com.lqviet.baseentity.repository;

import com.lqviet.baseentity.entities.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Base repository interface that provides common CRUD operations and soft delete functionality
 * for all entities that extend BaseEntity.
 * The BaseRepository only supports entities that have a Long ID type.
 * Other ID can have support in the future.
 * @param <T> the entity type that extends BaseEntity
 * @author Le Quoc Viet
 * @version 1.0.0
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID> extends JpaRepository<T, Long> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = false")
    Optional<T> findByIdNotDeleted(@Param("id") Long id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false ORDER BY e.id")
    List<T> findAllActive();

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false")
    Page<T> findAllActive(Pageable pageable);

    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt BETWEEN :startDate AND :endDate AND e.isDeleted = false ORDER BY e.createdAt")
    List<T> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt BETWEEN :startDate AND :endDate AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<T> findByUpdatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM #{#entityName} e WHERE e.createdBy = :createdBy AND e.isDeleted = false ORDER BY e.createdAt DESC")
    List<T> findByCreatedBy(@Param("createdBy") String createdBy);

    @Query("SELECT e FROM #{#entityName} e WHERE e.lastModifiedBy = :lastModifiedBy AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<T> findByLastModifiedBy(@Param("lastModifiedBy") String lastModifiedBy);

    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt >= :cutoffDate AND e.isDeleted = false ORDER BY e.createdAt DESC")
    List<T> findRecentlyCreated(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt >= :cutoffDate AND e.isDeleted = false ORDER BY e.updatedAt DESC")
    List<T> findRecentlyUpdated(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = true ORDER BY e.updatedAt DESC")
    List<T> findAllDeleted();

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = true")
    Page<T> findAllDeleted(Pageable pageable);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = true, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.isDeleted = false")
    int softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = false, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.isDeleted = true")
    int restoreById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = true, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id IN :ids AND e.isDeleted = false")
    int softDeleteByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = false, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id IN :ids AND e.isDeleted = true")
    int restoreByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.isDeleted = true AND e.updatedAt < :cutoffDate")
    int permanentlyDeleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.isDeleted = false")
    long countActive();

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.isDeleted = true")
    long countDeleted();

    @Query("SELECT COUNT(e) FROM #{#entityName} e")
    long countTotal();

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = false")
    boolean existsByIdNotDeleted(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.createdBy = :createdBy AND e.isDeleted = false")
    boolean existsByCreatedBy(@Param("createdBy") String createdBy);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = true")
    boolean isSoftDeleted(@Param("id") Long id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids AND e.isDeleted = false ORDER BY e.id")
    List<T> findByIdIn(@Param("ids") List<Long> ids);

    // Default methods for convenience
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

    default boolean softDelete(T entity) {
        if (entity != null && entity.getId() != null) {
            return softDeleteById(entity.getId()) > 0;
        }
        return false;
    }

    default boolean restore(T entity) {
        if (entity != null && entity.getId() != null) {
            return restoreById(entity.getId()) > 0;
        }
        return false;
    }
}
