package org.example.verify;

import com.google.gson.JsonObject;
import io.ipfs.multibase.Multibase;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.credential.Claim;
import org.example.sign.Proof;
import org.example.utils.JsonCanonicalizer;
import org.example.utils.VCUtil;
import org.example.vc.VerifiableCredential;

import java.io.IOException;
import java.security.*;

public class VerificationServiceImpl implements VerificationService {
    private final JsonObject mCredential;
    private final Proof mProof;

    public VerificationServiceImpl(VerifiableCredential credential) {
        mCredential = new JsonObject();
        mProof = credential.getProof();

        parseIntoJson(credential);
    }

    public boolean verifyCredential(PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(DigestUtils.sha256(encodeIntoString(mCredential)));
        return signature.verify(
                Multibase.decode(mProof.getCredntialJWTProof())
        );
    }

    public boolean verifyClaim(PublicKey publicKey, Claim claim) {
        return false;
    }

    private void parseIntoJson(VerifiableCredential credential) {
        mCredential.add("metadata", VCUtil.gson.toJsonTree(credential.getCredentialMetaData()));
        mCredential.add("credential", credential.getCredential().toJson());
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
