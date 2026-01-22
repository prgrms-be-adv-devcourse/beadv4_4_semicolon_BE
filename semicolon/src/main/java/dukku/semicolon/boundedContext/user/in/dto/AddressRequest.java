package dukku.semicolon.boundedContext.user.in.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AddressRequest {
    @NotBlank
    private String receiverName;

    @NotBlank
    private String receiverPhone;

    @NotBlank
    private String zipcode;

    @NotBlank
    private String address1;

    private String address2;
}
