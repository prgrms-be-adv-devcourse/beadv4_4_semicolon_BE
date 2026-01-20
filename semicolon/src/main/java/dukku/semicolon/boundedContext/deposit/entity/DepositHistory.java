package dukku.semicolon.boundedContext.deposit.entity;

import dukku.common.global.jpa.entity.BaseEntity;
import dukku.semicolon.boundedContext.deposit.entity.enums.DepositHistoryType;
import dukku.semicolon.shared.deposit.dto.DepositHistoryDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * 예치금 변동 이력 엔티티
 * 
 * <p>
 * 예치금의 모든 변동 내역을 기록한다.
 * 이력 엔티티는 생성 후 수정되지 않는다 (Immutable).
 */
@Entity
@Table(name = "deposit_histories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DepositHistory extends BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(updatable = false, nullable = false)
    private Integer id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "uuid", comment = "사용자 UUID")
    private UUID userUuid;

    @Column(updatable = false, nullable = false, comment = "변동 금액 (+/-)")
    private Long amount;

    @Column(updatable = false, nullable = false, comment = "변동 후 잔액 스냅샷")
    private Long balanceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, nullable = false, comment = "변동 유형")
    private DepositHistoryType type;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, columnDefinition = "uuid", comment = "관련 주문 상품 UUID (nullable)")
    private UUID orderItemUuid;

    @CreatedDate
    @Column(updatable = false, nullable = false, comment = "생성일")
    private LocalDateTime createdAt;

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return null; // 이력 엔티티는 수정되지 않음
    }

    // === 정적 팩토리 메서드 ===

    public static DepositHistory create(UUID userUuid, Long amount, Long balanceSnapshot,
            DepositHistoryType type, UUID orderItemUuid) {
        return DepositHistory.builder()
                .userUuid(userUuid)
                .amount(amount)
                .balanceSnapshot(balanceSnapshot)
                .type(type)
                .orderItemUuid(orderItemUuid)
                .build();
    }

    // === DTO 변환 ===

    public DepositHistoryDto toDto() {
        return DepositHistoryDto.builder()
                .id(this.id)
                .userUuid(this.userUuid)
                .amount(this.amount)
                .balanceSnapshot(this.balanceSnapshot)
                .type(this.type)
                .orderItemUuid(this.orderItemUuid)
                .createdAt(this.createdAt)
                .build();
    }
}
