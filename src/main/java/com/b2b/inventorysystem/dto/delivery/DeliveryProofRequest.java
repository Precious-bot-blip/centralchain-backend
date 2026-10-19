package com.b2b.inventorysystem.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryProofRequest {

    @NotBlank(message = "Signature URL is required")
    private String signatureUrl;

    @NotBlank(message = "Photo proof URL is required")
    private String photoProofUrl;
}