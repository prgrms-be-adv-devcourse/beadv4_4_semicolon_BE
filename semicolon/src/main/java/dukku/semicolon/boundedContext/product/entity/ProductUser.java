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
@AllArgsConstructor
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
            comment = "계정 상태"
    )
    private AccountStatus status;

    public static ProductUser create(UUID userUuid, String nickname) {
        return ProductUser.builder()
                .userUuid(userUuid)
                .nickname(nickname)
                .status(AccountStatus.ACTIVE)
                .build();
    }
}
