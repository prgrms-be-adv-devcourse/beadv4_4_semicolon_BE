package dukku.common.global.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseIdAndTime extends BaseEntity<Integer> {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @CreatedDate
    @Column(nullable = false, updatable = false, comment = "생성일")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(comment = "수정일")
    private LocalDateTime updatedAt;
}
