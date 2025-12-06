package com.ecommerce.SamCommerce.Payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//REQUEST FORMAT TO REQUEST ANY ORDER
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private Integer addressId;
    private String paymentMethod;
    private String pgName;
    private String pgPaymentId;
    private String pgStatus;
    private String pgResponseMessage;
}
