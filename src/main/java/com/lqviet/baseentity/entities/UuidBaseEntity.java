package com.lqviet.baseentity.entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Objects;
import java.util.UUID;

/**
 * Base entity class with UUID.
 * Best for: Distributed systems, microservices, external APIs
 * Features: Globally unique, no collision risk, database-agnostic
 *
 * @author Le Quoc Viet
 * @version 1.1.0
 */
@Setter
@Getter
@MappedSuperclass
public abstract class UuidBaseEntity extends AbstractBaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @Override
    public boolean isNew() {
        return this.id == null;
    }

    // UUID-specific helper methods
    public String getIdAsString() {
        return id != null ? id.toString() : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UuidBaseEntity that = (UuidBaseEntity) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with UUID: %s", this.getClass().getSimpleName(), getId());
    }
}
