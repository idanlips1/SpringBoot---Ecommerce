package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.payload.OrderResponse;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> placeOrder(@PathVariable String paymentMethod,
                                                @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        User user = authUtil.loggedInUser();
        OrderDTO orderDTO = orderService.placeOrder(
                user,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage());
        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }

    @GetMapping("/orders/users")
    public ResponseEntity<List<OrderDTO>> getUserOrders() {
        User user = authUtil.loggedInUser();
        List<OrderDTO> orders = orderService.getOrdersByUser(user.getEmail());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
        User user = authUtil.loggedInUser();
        OrderDTO orderDTO = orderService.getOrder(user.getEmail(), orderId);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        OrderResponse orderResponse = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        OrderDTO orderDTO = orderService.updateOrderStatus(orderId, status);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }
}
