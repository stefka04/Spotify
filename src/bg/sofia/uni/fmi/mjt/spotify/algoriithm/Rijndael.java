package bg.sofia.uni.fmi.mjt.spotify.algoriithm;

import bg.sofia.uni.fmi.mjt.spotify.exception.CipherException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Rijndael implements SymmetricBlockCipher {
    private static final String SALT = "12345678";
    private static final String PASSWORD = "spotify";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_SIZE_IN_BYTE = 256;
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private final SecretKey secretKey;

    public Rijndael() {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(PASSWORD.toCharArray(), SALT.getBytes(), ITERATION_COUNT, KEY_SIZE_IN_BYTE);
            SecretKey temp = factory.generateSecret(spec);
            secretKey = new SecretKeySpec(temp.getEncoded(), ENCRYPTION_ALGORITHM);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("A problem occurred while creating a secret key");
        }
    }

    @Override
    public String encrypt(String data) throws CipherException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                 | BadPaddingException e) {

            throw new CipherException("The encrypt operation cannot be completed successfully", e.getCause());
        }
    }

    @Override
    public String decrypt(String encryptedData) throws CipherException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                 | BadPaddingException e) {
            throw new CipherException("The decrypt operation cannot be completed successfully", e.getCause());
        }
    }
}
