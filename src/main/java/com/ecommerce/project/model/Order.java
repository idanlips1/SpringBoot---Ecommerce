package com.ecommerce.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String email;

    @ToString.Exclude
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDate orderDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private Double totalAmount;

    private String orderStatus;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;
}
