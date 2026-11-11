package com.centralchain.app.models;

public class SendMessageRequest {
    public long poId;
    public long senderId;
    public String messageText;

    public SendMessageRequest(long poId, long senderId, String messageText) {
        this.poId = poId;
        this.senderId = senderId;
        this.messageText = messageText;
    }
}
