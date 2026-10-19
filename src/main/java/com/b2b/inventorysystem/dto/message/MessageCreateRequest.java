package com.b2b.inventorysystem.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {

    @NotNull(message = "PO ID is required")
    private Integer poId;

    @NotNull(message = "Sender ID is required")
    private Integer senderId;

    @NotBlank(message = "Message text is required")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String messageText;
}