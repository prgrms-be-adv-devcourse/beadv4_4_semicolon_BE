package dukku.semicolon.boundedContext.user.in.dto;

import dukku.semicolon.boundedContext.user.entity.Address;
import lombok.Getter;

@Getter
public class AddressResponse {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String zipcode;
    private String address1;
    private String address2;
    private boolean isDefault;

    public static AddressResponse from(Address address) {
        AddressResponse res = new AddressResponse();
        res.id = address.getId();
        res.receiverName = address.getReceiverName();
        res.receiverPhone = address.getReceiverPhone();
        res.zipcode = address.getZipcode();
        res.address1 = address.getAddress1();
        res.address2 = address.getAddress2();
        res.isDefault = address.isDefault();
        return res;
    }

}
