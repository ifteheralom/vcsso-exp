package org.example;

import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.common.plaintexts.PlainText;
import org.cryptimeleon.craco.common.plaintexts.RingElementPlainText;
import org.cryptimeleon.craco.sig.Signature;
import org.cryptimeleon.craco.sig.SignatureKeyPair;
import org.cryptimeleon.craco.sig.SigningKey;
import org.cryptimeleon.craco.sig.VerificationKey;
import org.cryptimeleon.craco.sig.bbs.*;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zp;
import org.example.zkp.BBSzkProof;
import org.example.zkp.BBSzkProofScheme;

public class MainBBS {
    private final static int SECURITY_PARAM = 80;

    public static void main(String[] args) {
        BBSBKeyGen setup = new BBSBKeyGen();
        BBSBPublicParameter pp = setup.doKeyGen(SECURITY_PARAM, true);
        BBSBSignatureScheme bbsScheme = new BBSBSignatureScheme(pp);
        SignatureKeyPair<BBSBVerificationKey, BBSBSigningKey> keys = bbsScheme.generateKeyPair(2);


        MessageBlock messageBlock = new MessageBlock(
                new RingElementPlainText(pp.getZp().getUniformlyRandomElement()),
                new RingElementPlainText(pp.getZp().getUniformlyRandomElement()));


//        BBSABSignature signature = bbsScheme.sign(messageBlock, keys.getSigningKey());
//        boolean res =  bbsScheme.verify(messageBlock, signature, keys.getVerificationKey());
//        System.out.println("BBS res: " + res);

        BBSzkProofScheme zkProofScheme = new BBSzkProofScheme(pp);
        BBSABSignature signature = zkProofScheme.sign(messageBlock, keys.getSigningKey());
        boolean res =  zkProofScheme.verify(messageBlock, signature, keys.getVerificationKey());
        System.out.println("BBS res: " + res);
        BBSzkProof zkProof = zkProofScheme.generateProof(messageBlock, signature, keys.getSigningKey(), keys.getVerificationKey());
        zkProofScheme.verifyProof(zkProof, keys.getVerificationKey());
    }
}