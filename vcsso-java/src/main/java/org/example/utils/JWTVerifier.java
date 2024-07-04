package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.example.credential.Credential;
import org.example.credential.CredentialMetaData;

/**
 * This class is used to verify the JWT based VCs
 */
public class JWTVerifier {

    private JWTVerifier() {
    }

    /**
     *
     * @param vcJwt JWT token (VC) needs to be verified
     * @param hashAlgorithm Algorithm used to generate the JWT token
     * @return true if the verification success otherwise throws JWTVerificationException
     */
    public static boolean verifyJWT(String vcJwt, Algorithm hashAlgorithm) {
        com.auth0.jwt.interfaces.JWTVerifier verifier = JWT.require(hashAlgorithm)
                .ignoreIssuedAt()
                .build();
        verifier.verify(vcJwt);
        return true;
    }

    public static boolean verifySign(Credential credential, CredentialMetaData credentialMetaData, String signature, Algorithm algorithm) {


        return true;
    }
}