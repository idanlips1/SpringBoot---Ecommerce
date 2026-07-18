package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.payload.OrderResponse;
import com.ecommerce.project.payload.PaymentDTO;
import com.ecommerce.project.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public OrderDTO placeOrder(User user, Long addressId, String paymentMethod,
                                String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        String email = user.getEmail();
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user", user.getUserId()));

        if (cart.getCartItems().isEmpty()) {
            throw new APIException("Cart is empty");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment = paymentRepository.save(payment);

        Order order = new Order();
        order.setEmail(email);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted!");
        order.setAddress(address);
        order.setPayment(payment);
        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getProductQuantity() < cartItem.getQuantity()) {
                throw new APIException("Only " + product.getProductQuantity() + " units of "
                        + product.getProductName() + " available");
            }
            product.setProductQuantity(product.getProductQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setOrder(order);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItems.add(orderItemRepository.save(orderItem));
        }
        order.setOrderItems(orderItems);

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);

        return mapToDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByUser(String email) {
        return orderRepository.findAllByEmail(email).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public OrderDTO getOrder(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        if (!order.getEmail().equals(email)) {
            throw new APIException("Order does not belong to the logged-in user");
        }
        return mapToDTO(order);
    }

    @Override
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Order> orderPage = orderRepository.findAll(pageDetails);

        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(orderPage.getNumber());
        orderResponse.setPageSize(orderPage.getSize());
        orderResponse.setTotalElements(orderPage.getNumberOfElements());
        orderResponse.setTotalPages(orderPage.getTotalPages());
        orderResponse.setLastPage(orderPage.isLast());
        return orderResponse;
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        order.setOrderStatus(status);
        orderRepository.save(order);
        return mapToDTO(order);
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setEmail(order.getEmail());
        orderDTO.setOrderDate(order.getOrderDate());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setOrderStatus(order.getOrderStatus());
        orderDTO.setAddressId(order.getAddress().getAddressId());

        PaymentDTO paymentDTO = new PaymentDTO();
        Payment payment = order.getPayment();
        paymentDTO.setPaymentId(payment.getPaymentId());
        paymentDTO.setPaymentMethod(payment.getPaymentMethod());
        paymentDTO.setPgPaymentId(payment.getPgPaymentId());
        paymentDTO.setPgStatus(payment.getPgStatus());
        paymentDTO.setPgResponseMessage(payment.getPgResponseMessage());
        paymentDTO.setPgName(payment.getPgName());
        orderDTO.setPayment(paymentDTO);

        orderDTO.setOrderItems(order.getOrderItems().stream().map(item -> {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setOrderItemId(item.getOrderItemId());
            dto.setProductId(item.getProduct().getProductId());
            dto.setProductName(item.getProduct().getProductName());
            dto.setProductImage(item.getProduct().getProductImage());
            dto.setQuantity(item.getQuantity());
            dto.setDiscount(item.getDiscount());
            dto.setOrderedProductPrice(item.getOrderedProductPrice());
            return dto;
        }).toList());

        return orderDTO;
    }
}
