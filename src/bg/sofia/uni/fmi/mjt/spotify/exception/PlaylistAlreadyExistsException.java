package bg.sofia.uni.fmi.mjt.spotify.exception;

public class PlaylistAlreadyExistsException extends Exception {
    public PlaylistAlreadyExistsException(String message) {
        super(message);
    }
}