package org.example.sign;

import com.google.gson.JsonObject;
import io.ipfs.multibase.Multibase;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.credential.Credential;
import org.example.credential.CredentialMetaData;
import org.example.utils.JsonCanonicalizer;
import org.example.utils.VCUtil;

import java.io.IOException;
import java.net.URI;
import java.security.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class Ed25519Signature2020 {

    private final String TYPE = "Ed25519Signature2020";
    private Instant created;
    private URI verificationMethod;
    private String proofPurpose;
    private String proofValue;

    public Ed25519Signature2020(Instant created, Credential credential, CredentialMetaData credentialMetaData,
                                URI verificationMethod, String purpose, PrivateKey privateKey)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        this.created = created;
        this.verificationMethod = verificationMethod;
        this.proofPurpose = purpose;
        this.proofValue = sign(credential, credentialMetaData, privateKey);

    }

    public String getType() {
        return TYPE;
    }

    public Instant getCreated() {
        return created;
    }

    public URI getVerificationMethod() {
        return verificationMethod;
    }

    @Override
    public String toString() {
        return VCUtil.gson.toJson(this);
    }

    public String getProofPurpose() {
        return proofPurpose;
    }

    public String getProof() {
        return proofValue;
    }

    private String encodeIntoString(Credential credential, CredentialMetaData credentialMetaData, Proof proof) throws IOException {
        JsonObject signingContent = new JsonObject();
        signingContent.add("metadata", VCUtil.gson.toJsonTree(credentialMetaData));
        signingContent.add("credential", credential.toJson());

        if (proof == null) {
            JsonObject proofJson = VCUtil.gson.toJsonTree(this).getAsJsonObject();
            proofJson.remove("proofValue");
            signingContent.add("proof", proofJson);
        } else {
            JsonObject proofJson = VCUtil.gson.toJsonTree(proof).getAsJsonObject();
            proofJson.remove("proofValue");
            signingContent.add("proof", proofJson);
        }

        String encodedString = new JsonCanonicalizer(signingContent.toString()).getEncodedString();
        //System.out.println("String: " + encodedString);
        return encodedString;
    }

    private String sign(Credential credential, CredentialMetaData credentialMetaData, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(privateKey);
        signature.update(DigestUtils.sha256(encodeIntoString(credential, credentialMetaData, null)));
        return Multibase.encode(Multibase.Base.Base58BTC, signature.sign());
    }

//    public boolean verify(Credential credential, CredentialMetaData credentialMetaData, Proof proof, PublicKey publicKey) throws NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {
//        Signature signature = Signature.getInstance("Ed25519");
//        signature.initVerify(publicKey);
//        signature.update(DigestUtils.sha256(encodeIntoString(credential, credentialMetaData, proof)));
//        return signature.verify(Multibase.decode(proof.getProof().toString()));
//    }
}