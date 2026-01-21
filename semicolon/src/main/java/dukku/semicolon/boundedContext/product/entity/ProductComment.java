package dukku.semicolon.boundedContext.product.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 후순위 작업 : 테이블 미리 생성
 */
@Entity
@Table(
        name = "product_comments",
        indexes = {
                @Index(name = "idx_product_comments_product_created", columnList = "product_id, created_at"),
                @Index(name = "idx_product_comments_parent", columnList = "parent_id")
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductComment extends BaseIdAndUUIDAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, comment = "상품 ID")
    private Product product;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid", comment = "작성자 UUID")
    private UUID userUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", comment = "부모 댓글 ID(대댓글)")
    private ProductComment parent;

    @Column(nullable = false, columnDefinition = "TEXT", comment = "내용")
    private String content;

    @Column(comment = "삭제일(소프트 삭제)")
    private LocalDateTime deletedAt;
}
