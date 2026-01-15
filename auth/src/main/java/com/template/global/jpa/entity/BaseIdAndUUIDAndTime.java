package com.template.global.jpa.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@SuperBuilder
public abstract class BaseIdAndUUIDAndTime extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true, comment = "유저 UUID")
    private UUID uuid;
    @CreatedDate
    @Column(nullable = false, updatable = false, comment = "생성일")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(comment = "수정일")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UuidCreator.getTimeOrderedEpoch();
        }
    }
}