package bg.sofia.uni.fmi.mjt.spotify.user;

import bg.sofia.uni.fmi.mjt.spotify.algoriithm.Rijndael;
import bg.sofia.uni.fmi.mjt.spotify.algoriithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.spotify.exception.CipherException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundUserException;
import bg.sofia.uni.fmi.mjt.spotify.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.WeakPasswordException;

import java.security.NoSuchAlgorithmException;

public class UserService {
    private final UserRepositoryAPI userRepository;
    private final SymmetricBlockCipher symmetricBlockCipher;
    private static final int MIN_PASSWORD_LENGTH = 8;
    public UserService() {
        symmetricBlockCipher = new Rijndael();
        userRepository = new UserRepository();
    }

    public UserService(UserRepository userRepository, SymmetricBlockCipher symmetricBlockCipher) {
        this.userRepository = userRepository;
        this.symmetricBlockCipher = symmetricBlockCipher;
    }

    public void register(String email, String password) throws UserAlreadyExistsException,
        WeakPasswordException, CipherException, NoSuchAlgorithmException {

        if (userRepository.exists(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
        if (!checkForStrongPassword(password)) {
            throw new WeakPasswordException("Password is weak, should have at least 8 characters, including at least " +
                "one uppercase letter, one lowercase letter and one number");
        }
        String encryptedPassword = symmetricBlockCipher.encrypt(password);
        userRepository.save(email, encryptedPassword);
    }

    public void login(String email, String password)
        throws NotFoundUserException, CipherException {
        if (!userRepository.exists(email)) {
            throw new NotFoundUserException("Unable to login. User with email = " + email + " is not found. " +
                "Please try again or register");
        }

        String encryptedPassword = symmetricBlockCipher.encrypt(password);
        if (!userRepository.getUserPassword(email).equals(encryptedPassword)) {
            throw new NotFoundUserException("Unable to login. Wrong password. Please try again");
        }
    }

    private boolean checkForStrongPassword(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;

        for (int i = 0; i < password.length(); i++) {
            char current = password.charAt(i);
            if (Character.isUpperCase(current)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(current)) {
                hasLowerCase = true;
            } else if (Character.isDigit(current)) {
                hasDigit = true;
            }
        }
        return hasUpperCase && hasLowerCase && hasDigit;
    }
}
