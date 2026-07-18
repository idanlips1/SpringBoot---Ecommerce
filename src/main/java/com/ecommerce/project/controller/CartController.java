package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity) {
        User user = authUtil.loggedInUser();
        CartDTO cartDTO = cartService.addProductToCart(user, productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getUserCart() {
        User user = authUtil.loggedInUser();
        CartDTO cartDTO = cartService.getCartByUser(user);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> updateProductQuantity(@PathVariable Long productId, @PathVariable Integer quantity) {
        User user = authUtil.loggedInUser();
        CartDTO cartDTO = cartService.updateProductQuantityInCart(user, productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> carts = cartService.getAllCarts();
        return new ResponseEntity<>(carts, HttpStatus.OK);
    }
}
