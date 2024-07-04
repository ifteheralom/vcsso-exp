package org.cryptimeleon.craco.enc.params;

import org.cryptimeleon.craco.common.plaintexts.PlainText;
import org.cryptimeleon.craco.enc.EncryptionKeyPair;
import org.cryptimeleon.craco.enc.TestParams;
import org.cryptimeleon.craco.enc.asym.elgamal.ElgamalEncryption;
import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.enc.asym.elgamal.ElgamalPrivateKey;
import org.cryptimeleon.craco.enc.asym.elgamal.ElgamalPublicKey;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import org.cryptimeleon.math.structures.rings.zn.Zp;

import java.math.BigInteger;
import java.util.function.Supplier;

public class ElgamalParams {
    public static TestParams getParams() {
        Zp zp = new Zp(BigInteger.valueOf(72973));
        Group zpGroup = zp.asUnitGroup();

        ElgamalEncryption elgamalScheme = new ElgamalEncryption(zpGroup);

        Supplier<PlainText> supplier = () -> ((PlainText) new GroupElementPlainText(zpGroup.getUniformlyRandomElement()));

        EncryptionKeyPair validKeyPair = elgamalScheme.generateKeyPair();

        ElgamalPublicKey validPK = (ElgamalPublicKey) validKeyPair.getPk();
        ElgamalPrivateKey validSK = (ElgamalPrivateKey) validKeyPair.getSk();

        Zn expZn = zpGroup.getZn();
        ZnElement pow = validSK.getA().isOne() ? expZn.valueOf(5) :
                expZn.valueOf(1);
        ElgamalPrivateKey invalidSK = new ElgamalPrivateKey(zpGroup, validSK.getG(), pow);
        EncryptionKeyPair invalidKeyPair = new EncryptionKeyPair(validPK, invalidSK);

        return new TestParams(elgamalScheme, supplier, validKeyPair, invalidKeyPair);
    }
}
