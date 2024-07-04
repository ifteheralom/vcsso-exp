package org.example;

import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.JsonObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.example.credential.Credential;
import org.example.credential.CredentialMetaData;
import org.example.credential.CredentialSubject;
import org.example.sign.*;
import org.example.utils.VCUtil;
import org.example.vc.VerifiableCredential;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Main {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        CredentialSubject credentialSubject = new CredentialSubject();
        credentialSubject.addClaim("name", "Harry James Potter");
        credentialSubject.addClaim("job", "Dark Arts Research Fellow");
        credentialSubject.addClaim("university", "Hogwarts School of Witchcraft and Wizardry");
        credentialSubject.addClaim("id", "0123456789");
        credentialSubject.addClaim("email", "harry@hogwarts.magics");

        Credential credential = new Credential.Builder()
                .credentialSubject(credentialSubject)
                .build();
        Instant dateTime = Instant.now();
        CredentialMetaData credentialMetaData = new CredentialMetaData.Builder()
                .id("vc12345")
                .issuer("Hogwarts.magics")
                .additionalType("HogwartsCredential")
                .issuanceDate(dateTime)
                .expirationDate(dateTime.plus(3, ChronoUnit.DAYS))
                .build();

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        KeyPair ecdsKeyPair = keyGenerator.generateKeyPair();
        //Algorithm algorithm = Algorithm.ECDSA256(null, (ECPrivateKey) ecdsKeyPair.getPrivate());

        SigningService signingService = new SigningServiceImpl(credential, credentialMetaData, ecdsKeyPair.getPrivate());
        Proof proof = new ProofImpl(
                "SHA256withECDSA",
                Instant.now(),
                URI.create("linkToPublicKey"),
                "assertion",
                signingService.getCredentialJWT(),
                signingService.getCredentialJWTProof(),
                signingService.getClaimsProof()
        );
        
        VerifiableCredential vcWithEmbeddedProof = new VerifiableCredential.Builder()
                .credential(credential)
                .metadata(credentialMetaData)
                .proof(proof)
                .build();

        JsonObject jsonRepresentation = VCUtil.getJsonRepresentation(vcWithEmbeddedProof);
        System.out.println(jsonRepresentation);

//        vcWithEmbeddedProof.getCredential().getCredentialSubjects().get(0).getClaims()
//                .forEach(item -> {
//                    System.out.println(item.getValue() + " " + item.getSignature());
//                });
//
//        System.out.println(String.valueOf(Instant.now().toEpochMilli()));

//        String vcProofJwt = VCUtil.vcToJwT(vcWithEmbeddedProof, algorithm);
//        System.out.println("JWT: " + vcProofJwt);

//        boolean isVeriiedJWT = JWTVerifier.verifyJWT(
//                vcProofJwt,
//                Algorithm.ECDSA256((ECPublicKey) ecdsKeyPair.getPublic(), null)
//        );

//        boolean isVerifiedVC = new ECDSA256Signature().verify(
//                vcWithEmbeddedProof.getCredential(),
//                vcWithEmbeddedProof.getCredentialMetaData(),
//                vcWithEmbeddedProof.getProofs().get(0),
//                ecdsKeyPair.getPublic()
//        );

//        System.out.println("JWT Verified: " + isVeriiedJWT);
//        System.out.println("VC Verified: " + isVerifiedVC);
    }
}