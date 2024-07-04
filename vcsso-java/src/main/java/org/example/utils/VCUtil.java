package org.example.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.constants.Constants;
import org.example.credential.Credential;
import org.example.credential.CredentialMetaData;
import org.example.credential.CredentialSubject;
import org.example.sign.Proof;
import org.example.vc.VerifiableCredential;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public class VCUtil {

    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    public static Gson gson;

    static {
        gsonBuilder.registerTypeAdapter(Instant.class, new InstantSerializer());
        gson = gsonBuilder.create();
    }

    public static JsonObject getJsonRepresentation(VerifiableCredential verifiableCredential) {
        return getJsonRepresentation(
                verifiableCredential.getCredential(),
                verifiableCredential.getCredentialMetaData(),
                verifiableCredential.getProof(),
                false
        );
    }

    public static JsonObject getJsonRepresentation(Credential credential, CredentialMetaData credentialMetaData, Proof proof, boolean hideClaims) {
        JsonObject vc = new JsonObject();

        //CredentialMetaData credentialMetaData = verifiableCredential.getCredentialMetaData();
        List<URI> contexts = credentialMetaData.getContexts();
        if (contexts.size() == 1)
            vc.add(Constants.CONTEXT, gson.toJsonTree(contexts.get(0)));
        else if (contexts.size() > 1) {
            vc.add(Constants.CONTEXT, gson.toJsonTree(contexts));
        }

        vc.add(Constants.ID, gson.toJsonTree(credentialMetaData.getId()));
        vc.add(Constants.TYPE, gson.toJsonTree(credentialMetaData.getTypes()));
        vc.add(Constants.ISSUER, gson.toJsonTree(credentialMetaData.getIssuer()));
        vc.add(Constants.ISSUANCE_DATE, gson.toJsonTree(credentialMetaData.getIssuanceDate()));
        vc.add(Constants.EXPIRATION_DATE, gson.toJsonTree(credentialMetaData.getExpirationDate()));

        List<CredentialSubject> credentialSubjects = credential.getCredentialSubjects();
        if (credentialSubjects.size() == 1)
            if (hideClaims) {
                vc.add(Constants.CREDENTIAL_SUBJECT, credentialSubjects.get(0).toJsonHideClaims());
            } else {
                vc.add(Constants.CREDENTIAL_SUBJECT, credentialSubjects.get(0).toJson());
            }
        else {
            JsonArray jsonArray = new JsonArray();
            credentialSubjects.forEach(sub -> jsonArray.add(sub.toJson()));
            vc.add(Constants.CREDENTIAL_SUBJECT, jsonArray);
        }

        //Proof proof = verifiableCredential.getProof();
//        vc.add(Constants.PROOF, gson.toJsonTree((proof)));
        if (proof != null)
            vc.add(Constants.PROOF, proof.toJsonObject());

        return vc;
    }


    public static String vcToJwT(CredentialMetaData credentialMetaData, JsonObject jsonRepresentation, Algorithm hashAlgorithm) {
//        JsonObject jsonRepresentation = getJsonRepresentation(
//                verifiableCredential.getCredential(),
//                verifiableCredential.getCredentialMetaData(),
//                verifiableCredential.getProof(),
//                true);
//        jsonRepresentation.remove(Constants.PROOF);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("VC", jsonRepresentation);

        JWTCreator.Builder builder = JWT.create();
        builder.withPayload(jsonObject.toString());

        //CredentialMetaData credentialMetaData = verifiableCredential.getCredentialMetaData();
        builder.withIssuer(credentialMetaData.getIssuer().toString());
        builder.withIssuedAt(credentialMetaData.getIssuanceDate());
        builder.withExpiresAt(credentialMetaData.getExpirationDate());
        return builder.sign(hashAlgorithm);
    }
}
