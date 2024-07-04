package org.cryptimeleon.craco.enc.streaming;

import org.cryptimeleon.craco.common.utils.StreamUtil;
import org.cryptimeleon.craco.enc.EncryptionKeyPair;
import org.cryptimeleon.craco.enc.StreamingEncryptionScheme;
import org.cryptimeleon.craco.enc.streaming.params.StreamingAESParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

@RunWith(value = Parameterized.class)
public class StreamingEncryptionSchemeTest {

    private static SecureRandom RANDOM = new SecureRandom();

    private static int LENGTH = 18 * 1024;

    private StreamingEncryptionScheme encryptionScheme;

    private EncryptionKeyPair keyPair;

    public StreamingEncryptionSchemeTest(StreamingEncryptionSchemeParams params) {
        this.encryptionScheme = params.getEncryptionScheme();
        this.keyPair = params.getKeyPair();
    }

    @Test
    public void testDeprecatedStreamingEncryptDecrypt() {
        try {
            System.out
                    .println("Testing the encrypt(InputStream plainIn, OutputStream cipherOut, EncryptionKey pk) for "
                            + encryptionScheme.getClass().getName());
            // Generate new random bytes to be decrypted
            byte[] randomBytes = new byte[LENGTH];
            RANDOM.nextBytes(randomBytes);
            // create a buffered input stream that reads the bytes
            ByteArrayInputStream plainIn = new ByteArrayInputStream(randomBytes);
            // stream to write to
            ByteArrayOutputStream cipherOut = new ByteArrayOutputStream();
            // write to stream
            encryptionScheme.encrypt(plainIn, cipherOut, keyPair.getPk());
            System.out.println("Testing the decrypt(InputStream cipherIn, OutputStream plainOut, DecryptionKey sk) for "
                            + encryptionScheme.getClass().getName());
            // create an input stream from the output
            InputStream cipherIn = new ByteArrayInputStream(cipherOut.toByteArray());
            // write it back into a byte array
            ByteArrayOutputStream plainOut = new ByteArrayOutputStream();
            // decrypt it
            encryptionScheme.decrypt(cipherIn, plainOut, keyPair.getSk());
            System.out.println("Asserting the results...");
            assertArrayEquals(randomBytes, plainOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testEncryptInputStreamAndDecryptInputStream() {
        try {
            System.out.println("Testing the encrypt(InputStream plainIn, EncryptionKey pk) for "
                    + encryptionScheme.getClass().getName());
            // Generate new random bytes to be encrypted and decrypted
            byte[] randomBytes = new byte[LENGTH];
            RANDOM.nextBytes(randomBytes);
            // write the encrypted bytes to an input stream
            ByteArrayInputStream plainBytesIn = new ByteArrayInputStream(randomBytes);
            ByteArrayOutputStream plainBytesOut = new ByteArrayOutputStream(LENGTH);
            try (InputStream encryptedInputStream = encryptionScheme.encrypt(plainBytesIn, keyPair.getPk())) {
                System.out.println("Testing the decrypt(InputStream cipherIn, DecryptionKey sk) for "
                        + encryptionScheme.getClass().getName());
                // decrypt the bytes from the input stream
                try (InputStream decryptedCiphertext = encryptionScheme.decrypt(encryptedInputStream, keyPair.getSk())) {
                    StreamUtil.copy(decryptedCiphertext, plainBytesOut);
                }
            }

            System.out.println("Asserting the results...");
            assertArrayEquals(randomBytes, plainBytesOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCreateEncryptorAndCreateDecryptor() {
        try {
            System.out.println("Testing the createEncryptor(OutputStream cipherOut, EncryptionKey pk) for "
                    + encryptionScheme.getClass().getName());
            // Generate new random bytes to be decrypted
            byte[] randomBytes = new byte[LENGTH];
            RANDOM.nextBytes(randomBytes);
            // create an output stream to write the random bytes to
            ByteArrayOutputStream cipherOut = new ByteArrayOutputStream();
            ByteArrayOutputStream plainOut = new ByteArrayOutputStream();
            // let encryptingOut write the encrypted bytes to cipherOut
            // important to close the encrypting output stream before decrypting else not all will be written out
            try (OutputStream encryptingOut = encryptionScheme.createEncryptor(cipherOut, keyPair.getPk())) {
                encryptingOut.write(randomBytes);
            }
            System.out.println("Testing the createDecryptor(OutputStream plainOut, DecryptionKey sk) for "
                    + encryptionScheme.getClass().getName());
            // let decryptingOut write the decrypted bytes to plainOut
            try (OutputStream decryptingOut = encryptionScheme.createDecryptor(plainOut, keyPair.getSk())) {
                decryptingOut.write(cipherOut.toByteArray());
            }
            System.out.println("Asserting the results...");
            assertArrayEquals(randomBytes, plainOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<StreamingEncryptionSchemeParams> data() {
        ArrayList<StreamingEncryptionSchemeParams> toReturn = new ArrayList<>();
        toReturn.addAll(Arrays.asList(StreamingAESParams.getParams()));
        return toReturn;
    }
}

