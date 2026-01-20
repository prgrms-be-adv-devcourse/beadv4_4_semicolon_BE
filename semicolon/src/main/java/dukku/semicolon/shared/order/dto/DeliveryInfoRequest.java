package dukku.semicolon.shared.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeliveryInfoRequest {
    @Size(max = 50)
    @NotBlank
    String carrierName;
    @Size(max = 20)
    @NotBlank
    String carrierCode;
    @Size(max = 50)
    @NotBlank
    String trackingNumber;
}
