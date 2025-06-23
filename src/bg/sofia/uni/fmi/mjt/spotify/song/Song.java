package bg.sofia.uni.fmi.mjt.spotify.song;

import java.util.Objects;

public class Song {
    private final String name;
    private final String singerName;
    private int playingTimes;

    public Song(String name, String singerName, int playingTimes) {
        this.name = name;
        this.singerName = singerName;
        this.playingTimes = playingTimes;
    }

    public void increasePlayingTimes() {
        playingTimes++;
    }

    public String getName() {
        return name;
    }

    public String getSingerName() {
        return singerName;
    }

    public int getPlayingTimes() {
        return playingTimes;
    }

    @Override
    public String toString() {
        return "Song: " + name + " By " + singerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song song)) return false;
        return Objects.equals(name, song.name) && Objects.equals(singerName, song.singerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, singerName);
    }
}
