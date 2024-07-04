package org.example;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialContexts;
import com.danubetech.verifiablecredentials.jwt.FromJwtConverter;
import com.danubetech.verifiablecredentials.jwt.JwtVerifiableCredential;
import com.danubetech.verifiablecredentials.jwt.JwtVerifiablePresentation;
import com.danubetech.verifiablecredentials.jwt.ToJwtConverter;
import com.nimbusds.jose.JOSEException;
import foundation.identity.jsonld.JsonLDUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.net.URI;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class SampleW3CVCTestRun {
    public static void main(String[] args) throws DecoderException, JOSEException, ParseException {
        Map<String, Object> claims = new LinkedHashMap<>();
        Map<String, Object> degree = new LinkedHashMap<String, Object>();
        degree.put("name", "Bachelor of Science and Arts");
        degree.put("type", "BachelorDegree");
        claims.put("college", "Test University");
        claims.put("degree", degree);

        CredentialSubject credentialSubject = CredentialSubject.builder()
                .id(URI.create("did:example:ebfeb1f712ebc6f1c276e12ec21"))
                .claims(claims)
                .build();

        var dateTime = LocalDateTime.now();
        var dateTimeExpiry = dateTime.plusYears(1);
        VerifiableCredential verifiableCredential = VerifiableCredential.builder()
                .context(VerifiableCredentialContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_EXAMPLES_V1)
                .type("UniversityDegreeCredential")
                .id(URI.create("http://example.edu/credentials/3732"))
                .issuer(URI.create("did:example:76e12ec712ebc6f1c221ebfeb1f"))
                .issuanceDate(Date.from(dateTime.toInstant(ZoneOffset.of("Z"))))
                .expirationDate(Date.from(dateTimeExpiry.toInstant(ZoneOffset.of("Z"))))
                .credentialSubject(credentialSubject)
                .build();

        byte[] testEd25519PrivateKey = Hex.decodeHex("984b589e121040156838303f107e13150be4a80fc5088ccba0b0bdc9b1d89090de8777a28f8da1a74e7a13090ed974d879bf692d001cddee16e4cc9f84b60580".toCharArray());

        JwtVerifiableCredential jwtVerifiableCredential = ToJwtConverter.toJwtVerifiableCredential(verifiableCredential);
        System.out.println("" + FromJwtConverter.fromJwtVerifiableCredential(jwtVerifiableCredential).toJson(true));

        //String jwtPayload = jwtVerifiableCredential.getPayload().toString();
        //System.out.println(jwtPayload);

        String jwtString = jwtVerifiableCredential.sign_Ed25519_EdDSA(testEd25519PrivateKey);
        //System.out.println(jwtString);
        JwtVerifiablePresentation jwtVerifiablePresentation = JwtVerifiablePresentation.fromCompactSerialization(jwtString);
        //String jwtPayload2 = jwtVerifiablePresentation.getPayload().toString();
        //System.out.println(jwtPayload2);
        // String jwtString2 = jwtVerifiablePresentation.sign_Ed25519_EdDSA(testEd25519PrivateKey);
        //System.out.println(jwtString2);

        System.out.println("" + FromJwtConverter.fromJwtVerifiablePresentation(jwtVerifiablePresentation).toJson(true));
    }
}