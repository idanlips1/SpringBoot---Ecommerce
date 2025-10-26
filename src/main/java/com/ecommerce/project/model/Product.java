package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    @NotBlank
    @Size(min = 2, message = "Product must contain at least 2 letters")
    private String productName;
    @NotBlank
    @Size(min = 6, message = "Product description must contain at least 6 characters")
    private String productDescription;
    private String productImage;

    @PositiveOrZero
    private double productPrice;
    private double specialPrice;
    private double discount;
    private Integer productQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
