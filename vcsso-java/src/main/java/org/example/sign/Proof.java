package org.example.sign;

import com.google.gson.JsonObject;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public interface Proof {
    String getType();

    Instant getCreated();

    URI getVerificationMethod();

    String getProofPurpose();

    String getCredentialJWT();

    String getCredntialJWTProof();

    Map<String, String> getClaimsProof();

    JsonObject toJsonObject();
}