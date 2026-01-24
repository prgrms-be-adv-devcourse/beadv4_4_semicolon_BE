package dukku.semicolon.boundedContext.deposit.entity;

import dukku.common.global.jpa.entity.BaseEntity;
import dukku.semicolon.boundedContext.deposit.exception.NotEnoughDepositException;
import dukku.semicolon.shared.deposit.dto.DepositDto;
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

/**
 * 예치금 잔액 엔티티
 * 
 * <p>
 * 사용자별 예치금 잔액을 관리하는 엔티티.
 * user_uuid를 PK로 사용하여 사용자당 하나의 예치금 계좌를 운영한다.
 */
@Entity
@Table(name = "deposits")
@EntityListeners(AuditingEntityListener.class)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Deposit extends BaseEntity<UUID> {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "uuid", comment = "사용자 UUID (PK이자 소유자 식별자)")
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false, unique = true, columnDefinition = "uuid", comment = "예치금 계좌 UUID")
    private UUID depositUuid;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "bigint default 0", comment = "예치금 잔액")
    private Long balance = 0L;

    @Version
    @Builder.Default
    @Column(nullable = false, comment = "낙관적 락 버전")
    private Integer version = 0;

    @CreatedDate
    @Column(updatable = false, nullable = false, comment = "생성일")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(comment = "최종 수정일")
    private LocalDateTime updatedAt;

    @Override
    public UUID getId() {
        return userUuid;
    }

    // === 정적 팩토리 메서드 ===

    public static Deposit create(UUID userUuid) {
        return Deposit.builder()
                .userUuid(userUuid)
                .depositUuid(UUID.randomUUID())
                .balance(0L)
                .build();
    }

    // === 도메인 로직 ===

    /**
     * 잔액 증가 (충전/환불 등)
     */
    public void addBalance(Long amount) {
        if (amount == null || amount < 0L)
            return;
        this.balance += amount;
    }

    /**
     * 잔액 차감 (사용/출금 등)
     */
    public void subtractBalance(Long amount) {
        if (amount == null || amount < 0L)
            return;
        if (this.balance < amount) {
            throw new NotEnoughDepositException();
        }
        this.balance -= amount;
    }

    // === DTO 변환 ===

    public DepositDto toDto() {
        return DepositDto.builder()
                .userUuid(this.userUuid)
                .depositUuid(this.depositUuid)
                .balance(this.balance)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
