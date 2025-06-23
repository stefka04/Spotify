package bg.sofia.uni.fmi.mjt.spotify.song;

import java.io.Serializable;

public class SerializableEncoding implements Serializable {
    private String name;

    public SerializableEncoding(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
