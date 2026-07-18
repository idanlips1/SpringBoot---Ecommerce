package com.ecommerce.project.repositories;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.product.productId = :productId AND ci.cart.cartId = :cartId")
    CartItem findByProductIdAndCartId(@Param("productId") Long productId, @Param("cartId") Long cartId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
    void deleteCartItemByProductIdAndCartId(@Param("cartId") Long cartId, @Param("productId") Long productId);
}
