package bg.sofia.uni.fmi.mjt.spotify.user;

import bg.sofia.uni.fmi.mjt.spotify.algoriithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.spotify.exception.CipherException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundUserException;
import bg.sofia.uni.fmi.mjt.spotify.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.WeakPasswordException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final String userEmail = "test@email";
    private static final String userPassword = "testPassword6";

    @Mock
    private UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    @Mock
    private SymmetricBlockCipher symmetricBlockCipherMock = Mockito.mock(SymmetricBlockCipher.class);
    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterNewUser() throws UserAlreadyExistsException, WeakPasswordException, CipherException, NoSuchAlgorithmException {
        String encryptedPassword = "encrypt";

        when(userRepositoryMock.exists(userEmail)).thenReturn(false);
        when(symmetricBlockCipherMock.encrypt(userPassword)).thenReturn(encryptedPassword);

        userService.register(userEmail, userPassword);

        verify(userRepositoryMock).save(userEmail, encryptedPassword);
    }

    @Test
    void testRegisterWhenUserAlreadyExists() {
        when(userRepositoryMock.exists(userEmail)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(userEmail, userPassword),
            "UserAlreadyExistsException expected to be thrown when user with email = " + userEmail +
                " already exists");

        verify(userRepositoryMock, never()).save(userEmail, userPassword);
    }

    @Test
    void testRegisterWhenPasswordIsWeak() {
        String userWeakPassword = "weakPassword";

        assertThrows(WeakPasswordException.class, () -> userService.register(userEmail,  userWeakPassword),
            "WeakPasswordException expected to be thrown when password is weak");

        verify(userRepositoryMock, never()).save(userEmail, null);
    }

    @Test
    void testRegisterWhenPasswordIsWeakLessLength() {
        String userWeakPassword = "0000";

        assertThrows(WeakPasswordException.class, () -> userService.register(userEmail,  userWeakPassword),
            "WeakPasswordException expected to be thrown when password is with length < 8");

        verify(userRepositoryMock, never()).save(userEmail, null);
    }


    @Test
    void testLogin() throws CipherException, NotFoundUserException {
        String encryptedPassword = "sjId3ai@125f";

        when(userRepositoryMock.exists(userEmail)).thenReturn(true);
        when(userRepositoryMock.getUserPassword(userEmail)).thenReturn(encryptedPassword);
        when(symmetricBlockCipherMock.encrypt(userPassword)).thenReturn(encryptedPassword);

        assertDoesNotThrow(() -> userService.login(userEmail, userPassword), "Unexpected exception was thrown");
    }

    @Test
    void testLoginWhenUserIsNotFound() {
        when(userRepositoryMock.exists(userEmail)).thenReturn(false);

        assertThrows(NotFoundUserException.class, () -> userService.login(userEmail, userPassword),
            "NotFoundUserException expected to be thrown when user with email = " + userEmail + " was not found");
    }

    @Test
    void testLoginWhenUserPasswordIsWrong() throws NotFoundUserException, CipherException {
        String encryptedPassword = "sjId3ai@125f";

        when(userRepositoryMock.exists(userEmail)).thenReturn(true);

        when(userRepositoryMock.getUserPassword(userEmail)).thenReturn(userPassword);
        when(symmetricBlockCipherMock.encrypt(userPassword)).thenReturn(encryptedPassword);

        assertThrows(NotFoundUserException.class, () -> userService.login(userEmail, userPassword),
            "NotFoundUserException expected to be thrown when password is wrong");
    }
}
