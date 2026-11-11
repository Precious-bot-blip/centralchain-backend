package com.b2b.inventorysystem.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryFailRequest {
    @NotBlank(message = "Fault party is required")
    private String faultParty; // "DRIVER", "SUPPLIER", or "BUYER"

    @NotBlank(message = "Failure reason is required")
    private String failureReason;
}