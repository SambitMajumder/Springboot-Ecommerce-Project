package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Payload.OrderDTO;

public interface OrderService {
    OrderDTO placeOrder(String email, Integer addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
}
