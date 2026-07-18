package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderDTO placeOrder(User user, Long addressId, String paymentMethod,
                         String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
    List<OrderDTO> getOrdersByUser(String email);
    OrderDTO getOrder(String email, Long orderId);
    OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    OrderDTO updateOrderStatus(Long orderId, String status);
}
