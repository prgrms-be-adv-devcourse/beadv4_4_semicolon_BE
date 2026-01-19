package dukku.semicolon.shared.user.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import dukku.semicolon.boundedContext.user.entity.type.UserStatus;
import jakarta.persistence.*;
import lombok.*;
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
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class SourceUser extends BaseUser {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true, comment = "유저 UUID")
    private UUID uuid;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(comment = "탈퇴일")
    @Setter(AccessLevel.PROTECTED)
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        // UUID v7 생성
        if (this.uuid == null) {
            this.uuid = UuidCreator.getTimeOrderedEpoch();
        }

        // Status Active 기본 생성
        if (this.getStatus() == null) {
            setStatus(UserStatus.ACTIVE);
        }
    }
}