package com.centralchain.app.models;

// proof of delivery links
public class ProofRequest {
    public String signatureUrl;
    public String photoProofUrl;

    public ProofRequest(String signatureUrl, String photoProofUrl) {
        this.signatureUrl = signatureUrl;
        this.photoProofUrl = photoProofUrl;
    }
}
