package bg.sofia.uni.fmi.mjt.spotify.exception;

public class SongAlreadyExistsException extends Exception {
    public SongAlreadyExistsException(String message) {
        super(message);
    }
}