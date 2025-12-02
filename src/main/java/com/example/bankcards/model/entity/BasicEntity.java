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

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identifier")
    @Column(nullable = false, name = "id", unique = true, columnDefinition = "BIGINT")
    private Long id;

    @NotNull
    @Comment("Format ISO 8601: YYYY-MM-DD hh:mm:ss.000000")
    @Column(nullable = false, name = "created_at", columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @NotNull
    @Comment("Format ISO 8601: YYYY-MM-DD hh:mm:ss.000000")
    @Column(nullable = false, name = "updated_at", columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updateAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        BasicEntity that = (BasicEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}