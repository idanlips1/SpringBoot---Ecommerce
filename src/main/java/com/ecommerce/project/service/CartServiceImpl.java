package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public CartDTO addProductToCart(User user, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(0.0);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (cartItemRepository.findByProductIdAndCartId(productId, cart.getCartId()) != null) {
            throw new APIException("Product " + product.getProductName() + " already in cart. Use update instead.");
        }
        if (product.getProductQuantity() < quantity) {
            throw new APIException("Only " + product.getProductQuantity() + " units of " + product.getProductName() + " available");
        }
        if (quantity <= 0) {
            throw new APIException("Quantity must be greater than 0");
        }

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setDiscount(product.getDiscount());
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItemRepository.save(cartItem);

        cart.getCartItems().add(cartItem);
        recomputeTotal(cart);
        cartRepository.save(cart);

        return mapToDTO(cart);
    }

    @Override
    public List<CartDTO> getAllCarts() {
        return cartRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public CartDTO getCart(User user, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        if (!cart.getUser().getUserId().equals(user.getUserId())) {
            throw new APIException("Cart does not belong to the logged-in user");
        }
        return mapToDTO(cart);
    }

    @Override
    public CartDTO getCartByUser(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user", user.getUserId()));
        return mapToDTO(cart);
    }

    @Override
    public CartDTO updateProductQuantityInCart(User user, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user", user.getUserId()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, cart.getCartId());
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }
        if (quantity <= 0) {
            throw new APIException("Quantity must be greater than 0 — use delete to remove the item");
        }
        if (product.getProductQuantity() < quantity) {
            throw new APIException("Only " + product.getProductQuantity() + " units of " + product.getProductName() + " available");
        }

        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        recomputeTotal(cart);
        cartRepository.save(cart);

        return mapToDTO(cart);
    }

    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, cartId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        recomputeTotal(cart);
        cartRepository.save(cart);

        return "Product removed from cart";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findByProductIdAndCartId(productId, cartId);
        if (cartItem == null) {
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());
        cartItemRepository.save(cartItem);

        recomputeTotal(cart);
        cartRepository.save(cart);
    }

    private void recomputeTotal(Cart cart) {
        double total = cart.getCartItems().stream()
                .mapToDouble(item -> item.getProductPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(total);
    }

    private CartDTO mapToDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getCartId());
        cartDTO.setTotalPrice(cart.getTotalPrice());
        cartDTO.setProducts(cart.getCartItems().stream().map(item -> {
            CartItemDTO dto = new CartItemDTO();
            dto.setProductId(item.getProduct().getProductId());
            dto.setProductName(item.getProduct().getProductName());
            dto.setProductImage(item.getProduct().getProductImage());
            dto.setQuantity(item.getQuantity());
            dto.setProductPrice(item.getProduct().getProductPrice());
            dto.setDiscount(item.getDiscount());
            dto.setSpecialPrice(item.getProductPrice());
            return dto;
        }).toList());
        return cartDTO;
    }
}
