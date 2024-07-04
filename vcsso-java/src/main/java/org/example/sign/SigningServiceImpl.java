package org.example.sign;

import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.JsonObject;
import io.ipfs.multibase.Multibase;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.constants.Constants;
import org.example.credential.Credential;
import org.example.credential.CredentialMetaData;
import org.example.credential.CredentialSubject;
import org.example.utils.JsonCanonicalizer;
import org.example.utils.VCUtil;

import java.io.IOException;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigningServiceImpl implements SigningService {
    private final JsonObject mCredential;
    private final CredentialMetaData mMetaData;
    private final List<CredentialSubject> mSubjects;
    private Map<String, String> mClaimsProof;
    private final String mCredentialJWT;
    private final String mCredentialJWTProof;
    private String mCredentialProof;

    public SigningServiceImpl(Credential credential, CredentialMetaData credentialMetaData, PrivateKey privateKey) {
        this.mCredential = new JsonObject();
        this.mSubjects = credential.getCredentialSubjects();
        this.mMetaData = credentialMetaData;

        this.mClaimsProof = new HashMap<>();
        try {
            mClaimsProof = signClaims(privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.mCredentialJWT = encodeIntoJWT(credential, credentialMetaData, privateKey);
        try {
            mCredentialJWTProof = signCredential(privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeIntoJWT(Credential credential, CredentialMetaData credentialMetaData, PrivateKey privateKey) {
        JsonObject jsonRepresentation = VCUtil.getJsonRepresentation(
                credential,
                credentialMetaData,
                null,
                true
        );
        jsonRepresentation.remove(Constants.PROOF);

        return VCUtil.vcToJwT(
                credentialMetaData,
                jsonRepresentation,
                Algorithm.ECDSA256(null, (ECPrivateKey) privateKey)
        );
    }

    @Override
    public String getCredentialJWT() {
        return mCredentialJWT;
    }

    public String getCredentialJWTProof() {
        return mCredentialJWTProof;
    }

    public Map<String, String> getClaimsProof() {
        return mClaimsProof;
    }

    private String signCredential(PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        //signature.update(DigestUtils.sha256(encodeIntoString(mCredential)));
        signature.update(DigestUtils.sha256(mCredentialJWT));
        return Multibase.encode(
                Multibase.Base.Base58BTC,
                signature.sign()
        );
    }

    private Map<String, String> signClaims(PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Map<String, String> claimsProof = new HashMap<>();
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);

        if(mSubjects.size() == 1) {
            CredentialSubject subject = mSubjects.get(0);
            subject.getClaims().forEach(item -> {
                try {
                    signature.update(DigestUtils.sha256(item.getValue()));
                    item.sign(
                            Multibase.encode(Multibase.Base.Base58BTC, signature.sign()),
                            true,
                            mMetaData
                    );
                    claimsProof.put(
                            item.getId(), item.getSignature()
                    );
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            });
        }

        return claimsProof;
    }

    private void parseIntoJson(Credential credential, CredentialMetaData credentialMetaData) {
        mCredential.add("metadata", VCUtil.gson.toJsonTree(credentialMetaData));
        mCredential.add("credential", credential.toJson());
    }

    private String encodeIntoString(JsonObject credential) throws IOException {
        /*
        JsonObject signingContent = new JsonObject();
        signingContent.add("metadata", VCUtil.gson.toJsonTree(credentialMetaData));
        signingContent.add("credential", credential.toJson());
         */

        /*
        JsonObject proofJson = VCUtil.gson.toJsonTree(this).getAsJsonObject();
        proofJson.remove("proofValue");
        signingContent.add("proof", proofJson);
         */

        return new JsonCanonicalizer(credential.toString()).getEncodedString();
    }
}
