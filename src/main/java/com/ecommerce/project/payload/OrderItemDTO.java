package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private double discount;
    private double orderedProductPrice;
}
