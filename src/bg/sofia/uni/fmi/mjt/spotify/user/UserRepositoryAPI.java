package bg.sofia.uni.fmi.mjt.spotify.user;

import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundUserException;

public interface UserRepositoryAPI {
    void save(String email, String password);

    boolean exists(String email);

    String getUserPassword(String email) throws NotFoundUserException;
}
