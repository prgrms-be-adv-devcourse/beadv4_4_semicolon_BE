package dukku.semicolon.shared.settlement.exception;

import dukku.common.global.exception.NotFoundException;

public class SettlementNotFoundException extends NotFoundException {
    public SettlementNotFoundException() {
        super("존재하지 않는 정산입니다.");
    }
}
