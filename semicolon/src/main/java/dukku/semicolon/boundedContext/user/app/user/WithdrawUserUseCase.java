package dukku.semicolon.boundedContext.user.app.user;

import dukku.semicolon.boundedContext.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawUserUseCase {

    private final UserSupport userSupport;

    public void withdraw(UUID userUuid) {
        User user = userSupport.getActiveUserByUuid(userUuid);
        user.withdraw();
    }
}
