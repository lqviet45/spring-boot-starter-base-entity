package com.lqviet.baseentity.repository;

import com.lqviet.baseentity.entities.StringBaseEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for entities with String ID.
 * Extends common repository and adds String-specific methods.
 *
 * @param <T> the type of the entity that extends StringBaseEntity
 * @author Le Quoc Viet
 * @version 1.1.0
 */
@NoRepositoryBean
public interface StringBaseRepository<T extends StringBaseEntity> extends CommonBaseRepository<T, String> {

    // String-specific convenience methods
    default boolean softDelete(T entity) {
        if (entity != null && entity.getId() != null && !entity.getId().trim().isEmpty()) {
            return softDeleteById(entity.getId()) > 0;
        }
        return false;
    }

    default boolean restore(T entity) {
        if (entity != null && entity.getId() != null && !entity.getId().trim().isEmpty()) {
            return restoreById(entity.getId()) > 0;
        }
        return false;
    }

    // String-specific methods
    @Query("SELECT e FROM #{#entityName} e WHERE UPPER(e.id) LIKE UPPER(:pattern) AND e.isDeleted = false ORDER BY e.id")
    List<T> findByIdLikeIgnoreCase(@Param("pattern") String pattern);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id LIKE :prefix% AND e.isDeleted = false ORDER BY e.id")
    List<T> findByIdStartingWith(@Param("prefix") String prefix);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id LIKE %:suffix AND e.isDeleted = false ORDER BY e.id")
    List<T> findByIdEndingWith(@Param("suffix") String suffix);

    @Query("SELECT e FROM #{#entityName} e WHERE LENGTH(e.id) = :length AND e.isDeleted = false ORDER BY e.id")
    List<T> findByIdLength(@Param("length") int length);
}
