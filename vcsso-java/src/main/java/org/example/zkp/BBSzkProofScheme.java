package org.example.zkp;

import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.common.plaintexts.PlainText;
import org.cryptimeleon.craco.common.plaintexts.RingElementPlainText;
import org.cryptimeleon.craco.sig.SigningKey;
import org.cryptimeleon.craco.sig.bbs.*;
import org.cryptimeleon.math.expressions.bool.LazyBoolEvaluationResult;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.RingGroup;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroup;
import org.cryptimeleon.math.structures.groups.debug.DebugGroup;
import org.cryptimeleon.math.structures.groups.debug.DebugGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.nopairing.HashIntoSecp256k1;
import org.cryptimeleon.math.structures.groups.elliptic.nopairing.Secp256k1;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zp;
import org.springframework.aop.scope.ScopedProxyUtils;

import java.util.Arrays;

public class BBSzkProofScheme extends BBSBSignatureScheme {
    private final BBSBPublicParameter pp;
    private GroupElement elementB;

    public BBSzkProofScheme(BBSBPublicParameter publicParameter) {
        super(publicParameter);
        this.pp = getPublicParameters();
    }

    public BBSABSignature sign(PlainText plainText, SigningKey secretKey) {
        if (!(plainText instanceof MessageBlock)) {
            throw new IllegalArgumentException("Not a valid plain text for this scheme");
        } else if (!(secretKey instanceof BBSBSigningKey)) {
            throw new IllegalArgumentException("Not a valid signing key for this scheme");
        } else {
            MessageBlock messageBlock = (MessageBlock)plainText;
            BBSBSigningKey sk = (BBSBSigningKey)secretKey;
            if (messageBlock.length() != sk.getNumberOfMessages()) {
                throw new IllegalArgumentException("Not a valid block size for this scheme");
            } else {
                Zp.ZpElement exponentX;
                do {
                    exponentX = this.pp.getZp().getUniformlyRandomElement();
                } while(exponentX.equals(sk.getExponentGamma().neg()));

                Zp.ZpElement exponentSPrime = this.pp.getZp().getUniformlyRandomElement();
                Zp.ZpElement resultExponent = this.pp.getZp().getOneElement();
                resultExponent = resultExponent.add(sk.getZiExponents()[0].mul(exponentSPrime));

                for(int i = 1; i <= sk.getNumberOfMessages(); ++i) {
                    resultExponent = resultExponent.add(sk.getZiExponents()[i].mul((Zp.ZpElement)((RingElementPlainText)messageBlock.get(i - 1)).getRingElement()));
                }

                GroupElement c = this.pp.getG1();
                c = c.pow(resultExponent);
                elementB = c;
                Zn.ZnElement exponent = exponentX.add(sk.getExponentGamma()).inv();
                GroupElement signatureElementA = c.pow(exponent).compute();
                return new BBSABSignature(signatureElementA, exponentX, exponentSPrime);
            }
        }
    }

    // BBS Signature: (A, e, s)
    public BBSzkProof generateProof(MessageBlock messageBlock, BBSABSignature signature, BBSBSigningKey signingKey, BBSBVerificationKey verificationKey) {
        Zp.ZpElement r1, r2, r3, sPrime, hiddenExponent, disclosedExponent;
        GroupElement elementAPrime, elementABar, elementD, proofHidden, proofDisclosed;

        // r1, r2 ⟵ ℤp
        r1 = pp.getZp().getUniformlyRandomElement();
        r2 = pp.getZp().getUniformlyRandomElement();

        // r3 ⟵ 1⁄r1
        r3 = r1.inv();

        // s' = s - r2 r3
        sPrime = r2.mul(r3);
        sPrime = sPrime.neg();
        sPrime = sPrime.add(signature.getExponentS());
//        Zp.ZpElement testSprime = (Zp.ZpElement) signature.getExponentS().sub(r2.mul(r3));
//        System.out.println("SPrime: " + sPrime.equals(testSprime));

        // A' ⟵ A^(r1)
        elementAPrime = signature.getElementA().pow(r1).computeSync();

        // A̅ ⟵ A'^(-e)B^(r1) = A'^(x)
        elementABar = elementAPrime.pow(signingKey.getExponentGamma()).computeSync();
//        GroupElement testABar = elementAPrime.pow(signature.getExponentX().neg()).op(elementB.pow(r1));
//        System.out.println("Abar: " + elementABar.equals(testABar));

        // d ⟵ B^(r1) h0(-r2), B ⟵ g1 h0^(s) (h1^m1 + h2^m2 + ...)
        Zp.ZpElement elementHZero = signingKey.getZiExponents()[0].mul(r2.neg());
        elementD = elementB.pow(r1)
                .pow(elementHZero)
                .computeSync();
        GroupElement testd = elementB.pow(r1);

        // T1
        hiddenExponent = pp.getZp().getOneElement();
        hiddenExponent = hiddenExponent.add(signingKey.getZiExponents()[0].mul(sPrime.neg()));
        hiddenExponent = hiddenExponent.add(signingKey.getZiExponents()[1].mul(((RingElementPlainText)messageBlock.get(0)).getRingElement().neg()));
        proofHidden = elementD.pow(r3).computeSync();
        proofHidden = proofHidden.pow(hiddenExponent);
        System.out.println("H: " + proofHidden.getRepresentation().hashCode());

        // T2
        disclosedExponent = pp.getZp().getOneElement();
        disclosedExponent = disclosedExponent.add(signingKey.getZiExponents()[2].mul(((RingElementPlainText)messageBlock.get(1)).getRingElement()));
        proofDisclosed = pp.getG1();
        proofDisclosed = proofDisclosed.pow(disclosedExponent);
        System.out.println("D: " + proofDisclosed.computeSync().getRepresentation().hashCode());

        LazyBoolEvaluationResult expr = proofHidden.isEqualTo(proofDisclosed).evaluateLazy();
        System.out.println("Grp Equal: " + expr.getResult());

        return new BBSzkProof(
                elementAPrime,
                elementABar,
                elementD,
                proofHidden
        );
    }

    public void verifyProof(BBSzkProof proof, BBSBVerificationKey verificationKey) {
        GroupElement g2 = pp.getG2();
        GroupElement rightHandSide = pp.getBilinearMap().apply(proof.getElementABar(), g2);
        GroupElement leftHandSide = pp.getBilinearMap().apply(proof.getElementAPrime(), verificationKey.getW());
        boolean res = leftHandSide.equals(rightHandSide);

        System.out.println("Proof " + res);
    }
}