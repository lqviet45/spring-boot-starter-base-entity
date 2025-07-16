package com.lqviet.baseentity.repository;

import com.lqviet.baseentity.entities.BaseEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for entities with Long ID.
 * Extends common repository and adds Long-specific convenience methods.
 * @param <T> the entity type that extends BaseEntity
 * @author Le Quoc Viet
 * @version 1.1.0
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends CommonBaseRepository<T, Long> {

    // Long-specific convenience methods
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

    // Optional: Long-specific queries if needed
    @Query("SELECT e FROM #{#entityName} e WHERE e.id BETWEEN :startId AND :endId AND e.isDeleted = false ORDER BY e.id")
    List<T> findByIdRange(@Param("startId") Long startId, @Param("endId") Long endId);
}