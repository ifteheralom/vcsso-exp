package org.example.sign;

import com.google.gson.JsonObject;
import org.example.constants.Constants;
import org.example.utils.VCUtil;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class ProofImpl implements Proof {
    private final String mType;
    private final Instant mCreated;
    private final URI mVerificationMethod;
    private final String mProofPurpose;
    private final String mCredentialJWT;
    private final String mCredntialJWTProof;
    private final Map<String, String> mClaimsProof;

    public ProofImpl(
            String type,
            Instant created,
            URI verificationMethod,
            String proofPurpose,
            String credentialJWT,
            String credntialJWTProof,
            Map<String, String> claimsProof) {
        this.mType = type;
        this.mCreated = created;
        this.mVerificationMethod = verificationMethod;
        this.mProofPurpose = proofPurpose;
        this.mCredentialJWT = credentialJWT;
        this.mCredntialJWTProof = credntialJWTProof;
        this.mClaimsProof = claimsProof;
    }

    public String getType() {
        return mType;
    }

    public Instant getCreated() {
        return mCreated;
    }

    public URI getVerificationMethod() {
        return mVerificationMethod;
    }

    public String getProofPurpose() {
        return mProofPurpose;
    }

    @Override
    public String getCredentialJWT() {
        return mCredentialJWT;
    }

    public String getCredntialJWTProof() {
        return mCredntialJWTProof;
    }

    public Map<String, String> getClaimsProof() {
        return mClaimsProof;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = VCUtil.gson.toJsonTree(this).getAsJsonObject();
        jsonObject.remove(Constants.CLAIMS_PROOF);
        return jsonObject;
    }
}
