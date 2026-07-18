package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(User user, Long productId, Integer quantity);
    List<CartDTO> getAllCarts();
    CartDTO getCart(User user, Long cartId);
    CartDTO getCartByUser(User user);
    CartDTO updateProductQuantityInCart(User user, Long productId, Integer quantity);
    String deleteProductFromCart(Long cartId, Long productId);
    void updateProductInCarts(Long cartId, Long productId);
}
