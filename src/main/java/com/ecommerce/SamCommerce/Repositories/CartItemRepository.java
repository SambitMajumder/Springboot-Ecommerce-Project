package com.ecommerce.SamCommerce.Repositories;

import com.ecommerce.SamCommerce.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    //IT FILTERS THE PRODUCT INSIDE THE CART
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    CartItem findCartItemByProductIdAndCartId(Integer cartId, Integer productId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.cart.id = ?1 AND c.product.id = ?2")
    void deleteCartItemByProductIdAndCartId(Integer cartId, Integer productId);
}
