package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Payload.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
      CartDTO addProductToCart(Integer productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCart(String emailId, Integer cartId);

    @Transactional
    CartDTO updateProductQuantityInCart(Integer productId, Integer quantity);

    String deleteFromCart(Integer cartId, Integer productId);

    void updateProductInCarts(Integer cartId, int productID);
}
