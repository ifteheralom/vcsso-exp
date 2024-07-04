//package org.example;
//
//import com.auth0.jwt.algorithms.Algorithm;
//import com.google.gson.JsonObject;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.example.credential.Credential;
//import org.example.credential.CredentialMetaData;
//import org.example.credential.CredentialSubject;
//import org.example.sign.ECDSA256Signature;
//import org.example.sign.Proof;
//import org.example.sign.ProofManager;
//import org.example.utils.JWTVerifier;
//import org.example.utils.VCUtil;
//import org.example.vc.VerifiableCredential;
//
//import java.io.IOException;
//import java.net.URI;
//import java.security.GeneralSecurityException;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.Security;
//import java.security.interfaces.ECPrivateKey;
//import java.security.interfaces.ECPublicKey;
//import java.time.LocalDateTime;
//
//public class MainECDSA {
//    static {
//        Security.addProvider(new BouncyCastleProvider());
//    }
//
//    public static void main(String[] args) throws GeneralSecurityException, IOException {
//        CredentialSubject credentialSubject = new CredentialSubject();
//        credentialSubject.addClaim("name", "Thusitha Dayaratne");
//        credentialSubject.addClaim("job", "Research Fellow");
//        credentialSubject.addClaim("university", "Monash University");
//        credentialSubject.addClaim("id", "0123456789");
//
//        Credential credential = new Credential.Builder()
//                .credentialSubject(credentialSubject)
//                .build();
//        var dateTime = LocalDateTime.now();
//        CredentialMetaData credentialMetaData = new CredentialMetaData.Builder()
//                .id("vc12345")
//                .issuer("MonashUniversity")
//                .additionalType("MonashCredential")
//                .issuanceDate(dateTime)
//                .expirationDate(dateTime.plusYears(1))
//                .build();
//
//        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
//        KeyPair ecdsKeyPair = keyGenerator.generateKeyPair();
//        Algorithm algorithm = Algorithm.ECDSA256(null, (ECPrivateKey) ecdsKeyPair.getPrivate());
//        Proof proof = new ProofManager(dateTime, credential, credentialMetaData, URI.create("linkToPublicKey"),
//                "assertion", ecdsKeyPair.getPrivate());
//
//        VerifiableCredential vcWithEmbeddedProof = new VerifiableCredential.Builder()
//                .credential(credential)
//                .metadata(credentialMetaData)
//                .proof(proof)
//                .build();
//
//        JsonObject jsonRepresentation = VCUtil.getJsonRepresentation(vcWithEmbeddedProof);
//        System.out.println(jsonRepresentation.toString());
//
//        String vcProofJwt = VCUtil.vcToJwT(vcWithEmbeddedProof, algorithm);
//        System.out.println("JWT: " + vcProofJwt);
//
//        boolean isVeriiedJWT = JWTVerifier.verifyJWT(
//                vcProofJwt,
//                Algorithm.ECDSA256((ECPublicKey) ecdsKeyPair.getPublic(), null)
//        );
//
//        boolean isVerifiedVC = new ECDSA256Signature().verify(
//                vcWithEmbeddedProof.getCredential(),
//                vcWithEmbeddedProof.getCredentialMetaData(),
//                vcWithEmbeddedProof.getProofs().get(0),
//                ecdsKeyPair.getPublic()
//        );
//
//        System.out.println("JWT Verified: " + isVeriiedJWT);
//        System.out.println("VC Verified: " + isVerifiedVC);
//    }
//}