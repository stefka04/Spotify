package bg.sofia.uni.fmi.mjt.spotify.algorithm;

import bg.sofia.uni.fmi.mjt.spotify.algoriithm.Rijndael;
import bg.sofia.uni.fmi.mjt.spotify.algoriithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.spotify.exception.CipherException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RijndaelTest {
    private static SymmetricBlockCipher symmetricBlockCipher;

    @BeforeAll
    static void setUpTestCase() {
        symmetricBlockCipher = new Rijndael();
    }

    @Test
    void testEncrypt() throws CipherException {
        String textToEncrypt = "test";
        String encrypted = symmetricBlockCipher.encrypt(textToEncrypt);
        String resultDecrypted = symmetricBlockCipher.decrypt(encrypted);

        assertEquals(textToEncrypt, resultDecrypted, "Expected: " + textToEncrypt + " but was: " +
            resultDecrypted);
    }
}
