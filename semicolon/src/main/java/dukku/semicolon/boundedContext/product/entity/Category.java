package dukku.semicolon.boundedContext.product.entity;

import dukku.common.global.exception.BadRequestException;
import dukku.common.global.jpa.entity.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_parent_id", columnList = "parent_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseIdAndTime {

    @Column(nullable = false, length = 100, comment = "카테고리 이름")
    private String categoryName;

    @Column(nullable = false, comment = "깊이(1: 최상위, max: 3)")
    private int depth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", comment = "상위 카테고리")
    private Category parent;

    public static Category createRoot(String name) {
        return Category.builder()
                .categoryName(name)
                .depth(1)
                .build();
    }

    public static Category createChild(String name, Category parent) {
        if (parent == null) {
            throw new BadRequestException("parent는 필수입니다.");
        }

        return Category.builder()
                .categoryName(name)
                .parent(parent)
                .depth(parent.getDepth() + 1)
                .build();
    }
}
