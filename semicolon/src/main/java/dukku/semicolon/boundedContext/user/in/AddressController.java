package dukku.semicolon.boundedContext.user.in;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.user.app.address.AddAddressUseCase;
import dukku.semicolon.boundedContext.user.app.address.FindAddressUseCase;
import dukku.semicolon.boundedContext.user.in.dto.AddressRequest;
import dukku.semicolon.boundedContext.user.in.dto.AddressResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/addresse")
@RequiredArgsConstructor
public class AddressController {
    private final AddAddressUseCase addAddressUseCase;
    private final FindAddressUseCase findAddressUseCase;

    @GetMapping
    public List<AddressResponse> getMyAddresses() {
        UUID userUuid = UserUtil.getUserId();
        return findAddressUseCase.execute(userUuid);
    }

    @PostMapping
    public AddressResponse addAddress(
            @RequestBody @Valid AddressRequest request
    ) {
        UUID userUuid = UserUtil.getUserId();
        return addAddressUseCase.add(userUuid, request);
    }
}
