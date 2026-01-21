package dukku.semicolon.boundedContext.deposit.out;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import dukku.semicolon.boundedContext.deposit.entity.QDepositHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.UUID;

import static dukku.semicolon.boundedContext.deposit.entity.QDepositHistory.depositHistory;

@RequiredArgsConstructor
public class DepositHistoryRepositoryImpl implements DepositHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<DepositHistory> findHistoriesByCursor(UUID userUuid, Integer cursor, int size) {
        List<DepositHistory> results = queryFactory
                .selectFrom(depositHistory)
                .where(
                        userUuidEq(userUuid),
                        idLt(cursor))
                .orderBy(depositHistory.id.desc())
                .limit(size + 1)
                .fetch();

        boolean hasNext = false;
        if (results.size() > size) {
            results.remove(size);
            hasNext = true;
        }

        return new SliceImpl<>(results, PageRequest.of(0, size), hasNext);
    }

    private BooleanExpression userUuidEq(UUID userUuid) {
        return userUuid != null ? depositHistory.userUuid.eq(userUuid) : null;
    }

    private BooleanExpression idLt(Integer cursor) {
        return cursor != null ? depositHistory.id.lt(cursor) : null;
    }
}
