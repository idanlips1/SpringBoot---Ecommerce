package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);

    @Query("SELECT DISTINCT c FROM Cart c JOIN c.cartItems ci WHERE ci.product.productId = :productId")
    List<Cart> findCartsByProductId(@Param("productId") Long productId);
}
