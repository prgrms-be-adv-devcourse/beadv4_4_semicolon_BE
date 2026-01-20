package dukku.semicolon.boundedContext.product.entity;

import dukku.common.shared.product.type.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name = "product_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_users_nickname", columnNames = {"nickname"})
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductUser {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid", comment = "유저 UUID")
    private UUID userUuid;

    @Column(nullable = false, length = 50, comment = "닉네임")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "enum('ACTIVE','BLOCKED','DELETED')",
            comment = "계정 상태"
    )
    private AccountStatus status;

    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = AccountStatus.ACTIVE;
    }
}
