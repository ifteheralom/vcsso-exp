package org.cryptimeleon.craco.kem;

import org.cryptimeleon.craco.enc.DecryptionKey;
import org.cryptimeleon.craco.enc.EncryptionKey;
import org.cryptimeleon.craco.enc.EncryptionKeyPair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A generic test for {@link KeyEncapsulationMechanism}. It tests the scheme for valid key pair, i.e. containing a
 * secret key that satisfies the public keys policy, and a invalid key pair. Scheme and keys to test are defined by a
 * parameter class.
 *
 *
 */
@RunWith(value = Parameterized.class)
public class KeyEncapsulationMechanismTest {
    // type parameter intentionally left out to reuse the test for all kind of KEMs
    private KeyEncapsulationMechanism kem;
    private EncryptionKeyPair validKeyPair;
    private EncryptionKeyPair invalidKeyPair;

    public KeyEncapsulationMechanismTest(KeyEncapsulationMechanismTestParams params) {
        this.kem = params.kem;
        this.validKeyPair = params.validKeyPair;
        this.invalidKeyPair = params.invalidKeyPair;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<KeyEncapsulationMechanismTestParams> data() {
        ArrayList<KeyEncapsulationMechanismTestParams> schemes = new ArrayList<>();
        // Add schemes here
        return schemes;
    }

    @Test
    public void testEncapsDecrypt() {
        System.out.println("Testing valid encaps/decaps for " + kem.getClass().getName() + " ...");

        DecryptionKey sk = validKeyPair.getSk();
        EncryptionKey pk = validKeyPair.getPk();

        // Do an encapsulation/decapsulation run
        KeyEncapsulationMechanism.KeyAndCiphertext keyAndCiphertext = kem.encaps(pk);
        Object key = kem.decaps(keyAndCiphertext.encapsulatedKey, sk);
        assertTrue(key.equals(keyAndCiphertext.key));
    }

    @Test
    public void testFailEncryptDecrypt() {
        System.out.println("Testing invalid encaps/decaps for " + kem.getClass().getName() + " ...");

        DecryptionKey sk = invalidKeyPair.getSk();
        EncryptionKey pk = invalidKeyPair.getPk();

        // Do an encapsulation/decapsulation run
        KeyEncapsulationMechanism.KeyAndCiphertext keyAndCiphertext = kem.encaps(pk);
        try {
            Object key = kem.decaps(keyAndCiphertext.encapsulatedKey, sk);
            assertFalse(key.equals(keyAndCiphertext.key));
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException); // schemes should throw IllegalArgumentException if the
            // key is not fit to decrypt.
        }

    }
}
