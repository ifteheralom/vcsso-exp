package org.example.sign;

import java.util.Map;

public interface SigningService {
    String getCredentialJWT();

    String getCredentialJWTProof();

    Map<String, String> getClaimsProof();
}
