package com.lqviet.baseentity;

import com.lqviet.baseentity.config.AuditingConfig;
import com.lqviet.baseentity.entities.BaseEntity;
import com.lqviet.baseentity.utils.SoftDeleteUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Base Entity Spring Boot Starter library.
 * These tests verify library functionality without requiring a full Spring context.
 */
class BaseEntityLibraryTest {

    @Test
    void baseEntityHasCorrectDefaults() {
        // Test BaseEntity functionality without Spring context
        TestEntity entity = new TestEntity();

        // Test initial state
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getIsDeleted()).isFalse();

        // Test soft delete functionality
        entity.markAsDeleted();
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getIsDeleted()).isTrue();

        // Test restore functionality
        entity.restore();
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getIsDeleted()).isFalse();
    }

    @Test
    void baseEntityPrePersistWorks() {
        TestEntity entity = new TestEntity();

        // Simulate @PrePersist
        entity.onCreate();

        // Verify defaults are set
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getIsDeleted()).isFalse();
    }

    @Test
    void baseEntityPreUpdateWorks() {
        TestEntity entity = new TestEntity();
        entity.onCreate();

        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();

        // Wait a bit to ensure time difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate @PreUpdate
        entity.onUpdate();

        // Verify updatedAt is changed
        assertThat(entity.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void baseEntityEqualsAndHashCodeWork() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();

        // Test equals when both IDs are null
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());

        // Test equals when IDs are set
        entity1.setId(1L);
        entity2.setId(1L);
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());

        // Test not equals when IDs are different
        entity2.setId(2L);
        assertThat(entity1).isNotEqualTo(entity2);
    }

    @Test
    void baseEntityToStringWorks() {
        TestEntity entity = new TestEntity();
        entity.setId(1L);

        String toString = entity.toString();
        assertThat(toString).contains("TestEntity");
        assertThat(toString).contains("1");
    }

    @Test
    void softDeleteUtilsFilterActiveWorks() {
        // Create test entities
        TestEntity entity1 = new TestEntity();
        entity1.setName("Entity 1");

        TestEntity entity2 = new TestEntity();
        entity2.setName("Entity 2");
        entity2.markAsDeleted(); // Mark one as deleted

        List<TestEntity> entities = Arrays.asList(entity1, entity2);

        // Test filterActive
        List<TestEntity> active = SoftDeleteUtils.filterActive(entities);
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getName()).isEqualTo("Entity 1");
    }

    @Test
    void softDeleteUtilsFilterDeletedWorks() {
        TestEntity entity1 = new TestEntity();
        entity1.setName("Entity 1");

        TestEntity entity2 = new TestEntity();
        entity2.setName("Entity 2");
        entity2.markAsDeleted();

        List<TestEntity> entities = Arrays.asList(entity1, entity2);

        // Test filterDeleted
        List<TestEntity> deleted = SoftDeleteUtils.filterDeleted(entities);
        assertThat(deleted).hasSize(1);
        assertThat(deleted.get(0).getName()).isEqualTo("Entity 2");
    }

    @Test
    void softDeleteUtilsCanDeleteWorks() {
        TestEntity activeEntity = new TestEntity();
        TestEntity deletedEntity = new TestEntity();
        deletedEntity.markAsDeleted();

        assertThat(SoftDeleteUtils.canDelete(activeEntity)).isTrue();
        assertThat(SoftDeleteUtils.canDelete(deletedEntity)).isFalse();
        assertThat(SoftDeleteUtils.canDelete(null)).isFalse();
    }

    @Test
    void softDeleteUtilsCanRestoreWorks() {
        TestEntity activeEntity = new TestEntity();
        TestEntity deletedEntity = new TestEntity();
        deletedEntity.markAsDeleted();

        assertThat(SoftDeleteUtils.canRestore(activeEntity)).isFalse();
        assertThat(SoftDeleteUtils.canRestore(deletedEntity)).isTrue();
        assertThat(SoftDeleteUtils.canRestore(null)).isFalse();
    }

    @Test
    void softDeleteUtilsMarkAsDeletedWorks() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        List<TestEntity> entities = Arrays.asList(entity1, entity2);

        SoftDeleteUtils.markAsDeleted(entities);

        assertThat(entity1.isDeleted()).isTrue();
        assertThat(entity2.isDeleted()).isTrue();
    }

    @Test
    void softDeleteUtilsRestoreWorks() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        entity1.markAsDeleted();
        entity2.markAsDeleted();
        List<TestEntity> entities = Arrays.asList(entity1, entity2);

        SoftDeleteUtils.restore(entities);

        assertThat(entity1.isDeleted()).isFalse();
        assertThat(entity2.isDeleted()).isFalse();
    }

    @Test
    void softDeleteUtilsGetEligibleForPermanentDeletionWorks() {
        TestEntity entity1 = new TestEntity();
        entity1.markAsDeleted();
        entity1.setUpdatedAt(LocalDateTime.now().minusDays(10)); // Old deleted entity

        TestEntity entity2 = new TestEntity();
        entity2.markAsDeleted();
        entity2.setUpdatedAt(LocalDateTime.now().minusDays(1)); // Recent deleted entity

        TestEntity entity3 = new TestEntity();
        entity3.setUpdatedAt(LocalDateTime.now().minusDays(10)); // Old active entity

        List<TestEntity> entities = Arrays.asList(entity1, entity2, entity3);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(5);

        List<TestEntity> eligible = SoftDeleteUtils.getEligibleForPermanentDeletion(entities, cutoff);

        assertThat(eligible).hasSize(1);
        assertThat(eligible.get(0)).isEqualTo(entity1);
    }

    @Test
    void defaultAuditorProviderReturnsSystem() {
        AuditingConfig.DefaultAuditorAware auditor = new AuditingConfig.DefaultAuditorAware();
        Optional<String> currentAuditor = auditor.getCurrentAuditor();

        assertThat(currentAuditor).isPresent();
        assertThat(currentAuditor.get()).isEqualTo("system");
    }

    @Test
    void springSecurityAuditorProviderFallsBackToSystem() {
        // Test Spring Security auditor when no security context is available
        AuditingConfig.SpringSecurityAuditorAware auditor = new AuditingConfig.SpringSecurityAuditorAware();
        Optional<String> currentAuditor = auditor.getCurrentAuditor();

        assertThat(currentAuditor).isPresent();
        assertThat(currentAuditor.get()).isEqualTo("system");
    }

    @Test
    void auditingConfigCreatesCorrectBeans() {
        AuditingConfig config = new AuditingConfig();

        // Test that we can create the default auditor provider
        AuditorAware<String> auditorProvider = config.auditorProvider();
        assertThat(auditorProvider).isNotNull();
        assertThat(auditorProvider).isInstanceOf(AuditingConfig.DefaultAuditorAware.class);

        // Test that it returns the expected value
        Optional<String> auditor = auditorProvider.getCurrentAuditor();
        assertThat(auditor).isPresent();
        assertThat(auditor.get()).isEqualTo("system");
    }

    /**
     * Test entity for testing BaseEntity functionality
     */
    static class TestEntity extends BaseEntity {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        // Expose protected methods for testing
        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
        }

        // Expose getters for testing
        public LocalDateTime getCreatedAt() {
            return super.getCreatedAt();
        }

        public LocalDateTime getUpdatedAt() {
            return super.getUpdatedAt();
        }

        public Boolean getIsDeleted() {
            return super.getIsDeleted();
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            super.setUpdatedAt(updatedAt);
        }
    }
}