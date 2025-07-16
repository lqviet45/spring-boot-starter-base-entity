package com.lqviet.baseentity.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.util.Objects;

/**
 * Base entity class with String ID (natural keys).
 * Best for: Country codes, product SKUs, user handles, external references
 * Features: Human-readable IDs, natural business keys, manual control
 * * Note: Ensure IDs are unique and normalized to avoid collisions.
 *
 * @author Le Quoc Viet
 * @version 1.1.0
 */
@Getter
@MappedSuperclass
public abstract class StringBaseEntity extends AbstractBaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 100)
    private String id;

    public void setId(String id) {
        if (id != null) {
            this.id = normalizeId(id);
        } else {
            this.id = null;
        }
    }

    /**
     * Override this method in subclasses for custom ID normalization.
     * Default implementation: trim and uppercase
     */
    protected String normalizeId(String id) {
        return id.trim().toUpperCase();
    }

    @Override
    public boolean isNew() {
        return this.id == null || this.id.trim().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StringBaseEntity that = (StringBaseEntity) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getSimpleName(), getId());
    }
}
