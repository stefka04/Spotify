package bg.sofia.uni.fmi.mjt.spotify.command;

public enum CommandType {
    REGISTER("register"),
    LOGIN("login"),
    DISCONNECT("disconnect"),
    SEARCH("search"),
    TOP("top"),
    CREATE_PLAYLIST("create-playlist"),
    ADD_SONG_TO("add-song-to"),
    SHOW_PLAYLIST("show-playlist"),
    PLAY("play"),
    STOP("stop"),
    UNKNOWN("unknown");

    private final String value;

    CommandType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
