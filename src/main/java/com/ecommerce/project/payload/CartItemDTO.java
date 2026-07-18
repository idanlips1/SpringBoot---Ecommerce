package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private double productPrice;
    private double discount;
    private double specialPrice;
}
