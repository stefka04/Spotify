package bg.sofia.uni.fmi.mjt.spotify.algoriithm;

import bg.sofia.uni.fmi.mjt.spotify.exception.CipherException;

public interface SymmetricBlockCipher {
    /**
     * Encrypts the data and returns it
     *
     * @param data the data that should be encrypted
     * @throws CipherException if the encrypt/decrypt operation cannot be completed successfully
     */
    String encrypt(String data) throws CipherException;

    /**
     * Decrypts the data and returns it
     *
     * @param encryptedData the data that should be decrypted
     * @throws CipherException if the encrypt/decrypt operation cannot be completed successfully
     */
    String decrypt(String encryptedData) throws CipherException;
}
