package dukku.semicolon.boundedContext.user.entity;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 UUID */
    @Column(name = "user_uuid", nullable = false, length = 36)
    private java.util.UUID userUuid;

    private String receiverName;
    private String receiverPhone;

    private String zipcode;
    private String address1;
    private String address2;

    @Column(nullable = false)
    private boolean isDefault;

    @Builder
    public Address(
            java.util.UUID userUuid,
            String receiverName,
            String receiverPhone,
            String zipcode,
            String address1,
            String address2,
            boolean isDefault
    ) {
        this.userUuid = userUuid;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
        this.isDefault = isDefault;
    }

    public void changeDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void update(
            String receiverName,
            String receiverPhone,
            String zipcode,
            String address1,
            String address2
    ) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
    }
}
