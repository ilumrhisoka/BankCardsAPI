package com.example.bankcards.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity class that provides common fields for all persistent entities.
 * It includes an auto-generated ID, and timestamps for creation and last update.
 * This class is intended to be inherited by other entity classes.
 */
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BasicEntity {

    /**
     * Unique identifier for the entity.
     * Generated automatically using identity strategy (database handles auto-increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identifier")
    @Column(nullable = false, name = "id", unique = true, columnDefinition = "BIGINT")
    private Long id;

    /**
     * Timestamp indicating when the entity was created.
     * Automatically set upon creation and stored in ISO 8601 format.
     */
    @NotNull
    @Comment("Format ISO 8601: YYYY-MM-DD hh:mm:ss.000000")
    @Column(nullable = false, name = "created_at", columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp indicating when the entity was last updated.
     * Automatically updated on each modification and stored in ISO 8601 format.
     */
    @LastModifiedDate
    @NotNull
    @Comment("Format ISO 8601: YYYY-MM-DD hh:mm:ss.000000")
    @Column(nullable = false, name = "updated_at", columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updateAt;

    /**
     * Compares this BasicEntity to the specified object. The result is true if and only if
     * the argument is not null and is a BasicEntity object that has the same ID value as this object.
     *
     * @param o The object to compare this BasicEntity against.
     * @return true if the given object represents a BasicEntity equivalent to this BasicEntity, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        BasicEntity that = (BasicEntity) o;
        return id != null && id.equals(that.id);
    }

    /**
     * Returns a hash code for this BasicEntity. The hash code is based on the ID value.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}