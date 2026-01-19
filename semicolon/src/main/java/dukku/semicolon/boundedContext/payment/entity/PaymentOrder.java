package dukku.semicolon.boundedContext.payment.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentOrderStatus;
import dukku.semicolon.shared.payment.dto.PaymentOrderDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 결제 주문 엔티티 (Aggregate Root)
 * 
 * <p>
 * 주문(Order) Bounded Context의 결과물을 결제 도메인에서 관리하기 위한 엔티티.
 * {@code SourceOrder} (Order BC)의 레플리카 역할을 수행한다.
 * 
 * <p>
 * TODO: Order BC 구현 후, 공통 모듈의 {@code SourceOrder} 인터페이스를 상속받도록 리팩토링 예정.
 * 현재는 UUID를 통해 Order BC의 주문과 매핑된다.
 * 
 * @see Payment 실제 결제 정보 (1:N)
 */
@Entity
@Table(name = "payment_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class PaymentOrder extends BaseIdAndUUIDAndTime {

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid", comment = "결제 사용자 UUID")
    private UUID userUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "결제 주문 상태")
    private PaymentOrderStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "paymentOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    // === 정적 팩토리 메서드 ===

    /**
     * 주문 이벤트를 수신하여 결제 주문 레플리카를 생성한다.
     */
    public static PaymentOrder create(UUID orderUuid, UUID userUuid) {
        return PaymentOrder.builder()
                .userUuid(userUuid)
                .status(PaymentOrderStatus.PAID)
                .build();
    }

    // === DTO 변환 ===

    public PaymentOrderDto toDto() {
        return PaymentOrderDto.builder()
                .id(this.getId())
                .uuid(this.getUuid())
                .userUuid(this.userUuid)
                .status(this.status)
                .payments(this.payments.stream().map(Payment::toDto).toList())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
