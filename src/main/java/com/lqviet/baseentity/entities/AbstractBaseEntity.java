package com.lqviet.baseentity.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

// ============ ABSTRACT BASE WITH COMMON FIELDS ============

/**
 * Abstract base class containing all common auditing and soft delete fields.
 * This class contains the shared functionality but no ID definition.
 *
 * @author Le Quoc Viet
 * @version 1.1.0
 */
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
abstract class AbstractBaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // Common business methods
    public void markAsDeleted() {
        this.isDeleted = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public void restore() {
        this.isDeleted = false;
    }

    public abstract boolean isNew();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}