package dukku.semicolon.boundedContext.user.app.address;

import dukku.semicolon.boundedContext.user.entity.Address;
import dukku.semicolon.boundedContext.user.in.dto.AddressRequest;
import dukku.semicolon.boundedContext.user.in.dto.AddressResponse;
import dukku.semicolon.boundedContext.user.out.AddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AddAddressUseCase {
    private final AddressRepository addressRepository;

    @Transactional
    public AddressResponse add(UUID userUuid, AddressRequest request) {

        boolean isFirstAddress =
                addressRepository.countByUserUuid(userUuid) == 0;

        Address address = Address.builder()
                .userUuid(userUuid)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .zipcode(request.getZipcode())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .isDefault(isFirstAddress)
                .build();

        return AddressResponse.from(addressRepository.save(address));
    }
}
