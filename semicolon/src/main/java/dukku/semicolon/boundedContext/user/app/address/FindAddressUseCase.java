package dukku.semicolon.boundedContext.user.app.address;

import dukku.semicolon.boundedContext.user.in.dto.AddressResponse;
import dukku.semicolon.boundedContext.user.out.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindAddressUseCase {

    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> execute(UUID userUuid) {
        return addressRepository.findByUserUuid(userUuid)
                .stream()
                .map(AddressResponse::from)
                .toList();
    }
}

