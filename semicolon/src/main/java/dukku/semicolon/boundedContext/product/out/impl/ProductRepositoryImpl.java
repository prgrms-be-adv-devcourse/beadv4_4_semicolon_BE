package dukku.semicolon.boundedContext.product.out.impl;

import dukku.semicolon.boundedContext.product.out.CustomProductRepository;
import dukku.semicolon.shared.product.dto.cqrs.ProductStatDto;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements CustomProductRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkUpdateProductStats(List<ProductStatDto> stats) {
        String sql = "UPDATE product " +
                "SET view_count = ?, like_count = ?, comment_count = ? " +
                "WHERE id = ?";

        // JDBC Batch Update 실행 (쿼리를 묶어서 전송)
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                ProductStatDto dto = stats.get(i);
                ps.setLong(1, dto.viewCount());
                ps.setLong(2, dto.likeCount());
                ps.setLong(3, dto.commentCount());
                ps.setLong(4, dto.productId());
            }

            @Override
            public int getBatchSize() {
                return stats.size();
            }
        });
    }
}
