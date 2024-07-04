package org.example.credential;

import java.sql.Timestamp;
import java.time.Instant;

public class Claim {

    private String id;
    private final String name;
    private final Object value;
    private String signature;
    private boolean validity;
    private CredentialMetaData credentialMetaData;

    public Claim(String name, Object value) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Claim name cannot be null or empty");
        if (value == null)
            throw new IllegalArgumentException("Claim value cannot be null");

        this.name = name;
        this.value = value;

        this.id = null;
        this.signature = null;
        this.validity = false;
        this.credentialMetaData = null;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value.toString();
    }

    public String getSignature() {
        return signature;
    }

    public boolean getValidity() {
        return validity;
    }

    public String getId() {
        return id;
    }

    public CredentialMetaData getCredentialMetaData() {
        return credentialMetaData;
    }

    public void sign(String signature, boolean validity, CredentialMetaData metaData) {
        this.signature = signature;
        this.validity = validity;
        this.credentialMetaData  = metaData;
        this.id = String.valueOf(Instant.now().toEpochMilli());
    }
}