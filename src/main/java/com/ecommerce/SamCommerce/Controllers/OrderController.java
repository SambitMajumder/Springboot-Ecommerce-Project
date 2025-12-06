package com.ecommerce.SamCommerce.Controllers;

import com.ecommerce.SamCommerce.Payload.OrderDTO;
import com.ecommerce.SamCommerce.Payload.OrderRequestDTO;
import com.ecommerce.SamCommerce.Services.OrderService;
import com.ecommerce.SamCommerce.Util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod,
                                                @RequestBody OrderRequestDTO orderRequestDTO){
        String email = authUtil.loggedInEmail();
        OrderDTO orderDTO = orderService.placeOrder(email,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage());
        return new ResponseEntity<OrderDTO>(orderDTO, HttpStatus.CREATED);
    }
}
