package dukku.semicolon.shared.product.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateShopRequest {

    @Size(max = 500)
    private String intro;
}
