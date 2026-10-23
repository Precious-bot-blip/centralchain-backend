package com.b2b.inventorysystem.dto.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Integer messageId;
    private Integer poId;
    private String poNumber;

    private Integer senderId;
    private String senderName;
    private String senderRoleName;

    private String messageText;
    private LocalDateTime sentAt;
}