package dukku.semicolon.shared.order.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public final class AdminOrderApiDocs {
    private AdminOrderApiDocs() {
    }

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(name = "관리자 주문 관리 API", description = "관리자 전용 주문 조회 및 관리 기능")
    public @interface AdminOrderTag {
    }

    // 1. 전체 주문 목록 조회
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "전체 주문 목록 조회", description = "모든 사용자의 주문 내역을 검색하고 조회합니다.")
    public @interface FindAllOrders {
    }

    // 2. 특정 판매자 판매 내역 조회
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "특정 판매자 판매 내역 조회",
            description = "관리자가 특정 판매자의 판매 기록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
            }
    )
    public @interface FindSellerSalesHistory {
    }

    // 3. 정산용 데이터 조회
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "구매 확정(정산) 목록 조회", description = "정산 처리를 위해 기간 내 구매 확정된 항목을 조회합니다.")
    public @interface FindConfirmedItems {
    }
}