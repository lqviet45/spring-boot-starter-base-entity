package com.lqviet.baseentity.repository;

import com.lqviet.baseentity.entities.UuidBaseEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for entities with UUID ID.
 * Extends common repository and adds UUID-specific methods.
 *
 * @param <T> the type of entity that extends UuidBaseEntity
 *
 * @author Le Quoc Viet
 * @version 1.1.0
 */
@NoRepositoryBean
public interface UuidBaseRepository<T extends UuidBaseEntity> extends CommonBaseRepository<T, UUID> {

    // UUID-specific convenience methods
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

    // UUID-specific methods
    @Query("SELECT e FROM #{#entityName} e WHERE CAST(e.id AS string) = :idString AND e.isDeleted = false")
    Optional<T> findByIdStringNotDeleted(@Param("idString") String idString);

    @Query("SELECT e FROM #{#entityName} e WHERE CAST(e.id AS string) IN :idStrings AND e.isDeleted = false")
    List<T> findByIdStringsNotDeleted(@Param("idStrings") List<String> idStrings);
}
