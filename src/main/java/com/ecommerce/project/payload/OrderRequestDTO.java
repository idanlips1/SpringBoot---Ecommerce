package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    @NotNull
    private Long addressId;

    @NotBlank
    private String pgName;

    private String pgPaymentId;

    @NotBlank
    private String pgStatus;

    private String pgResponseMessage;
}
