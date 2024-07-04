package org.example.vc;

import org.example.credential.Credential;
import org.example.credential.CredentialMetaData;
import org.example.sign.Proof;

import java.util.ArrayList;
import java.util.List;

public class VerifiableCredential {
    private final CredentialMetaData credentialMetaData;
    private final Credential credential;
    private List<Proof> proofs;
    private final Proof proof;

    private VerifiableCredential(CredentialMetaData credentialMetaData, Credential credential, Proof proof) {
        this.credentialMetaData = credentialMetaData;
        this.credential = credential;
        this.proof = proof;
    }

    public CredentialMetaData getCredentialMetaData() {
        return credentialMetaData;
    }

    public Credential getCredential() {
        return credential;
    }

    public List<Proof> getProofs() {
        return proofs;
    }

    public Proof getProof() {
        return proof;
    }

    public static class Builder {
        private CredentialMetaData credentialMetaData;
        private Credential credential;
        private Proof proof;
        private final List<Proof> proofs = new ArrayList<>();

        public Builder metadata(CredentialMetaData metadata) {
            this.credentialMetaData = metadata;
            return this;
        }

        public Builder credential(Credential credential) {
            this.credential = credential;
            return this;
        }

        public Builder proofs(List<Proof> proofs) {
            this.proofs.addAll(proofs);
            return this;
        }

        public Builder proof(Proof proof) {
            this.proof = proof;
            return this;
        }

        public VerifiableCredential build() {
            if (credentialMetaData == null || credential == null)
                throw new RuntimeException("Metadata, credential and proofs cannot be null or empty");
            return new VerifiableCredential(credentialMetaData, credential, proof);
        }
    }
}