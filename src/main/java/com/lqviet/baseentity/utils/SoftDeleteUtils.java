package com.lqviet.baseentity.utils;

import com.lqviet.baseentity.entities.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for soft delete operations.
 *
 * @author Le Quoc Viet
 * @version 1.0.0
 */
public final class SoftDeleteUtils {

    private SoftDeleteUtils() {
        // Utility class
    }

    /**
     * Filter out soft-deleted entities from a list
     */
    public static <T extends BaseEntity> List<T> filterActive(List<T> entities) {
        return entities.stream()
                .filter(entity -> !entity.isDeleted())
                .collect(Collectors.toList());
    }

    /**
     * Filter only soft-deleted entities from a list
     */
    public static <T extends BaseEntity> List<T> filterDeleted(List<T> entities) {
        return entities.stream()
                .filter(BaseEntity::isDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Mark multiple entities as deleted
     */
    public static <T extends BaseEntity> void markAsDeleted(List<T> entities) {
        entities.forEach(BaseEntity::markAsDeleted);
    }

    /**
     * Restore multiple entities
     */
    public static <T extends BaseEntity> void restore(List<T> entities) {
        entities.forEach(BaseEntity::restore);
    }

    /**
     * Check if an entity is safe to delete (not already deleted)
     */
    public static <T extends BaseEntity> boolean canDelete(T entity) {
        return entity != null && !entity.isDeleted();
    }

    /**
     * Check if an entity can be restored (is currently deleted)
     */
    public static <T extends BaseEntity> boolean canRestore(T entity) {
        return entity != null && entity.isDeleted();
    }

    /**
     * Get entities that are eligible for permanent deletion (deleted before cutoff date)
     */
    public static <T extends BaseEntity> List<T> getEligibleForPermanentDeletion(
            List<T> entities, LocalDateTime cutoffDate) {
        return entities.stream()
                .filter(BaseEntity::isDeleted)
                .filter(entity -> entity.getUpdatedAt().isBefore(cutoffDate))
                .collect(Collectors.toList());
    }
}
