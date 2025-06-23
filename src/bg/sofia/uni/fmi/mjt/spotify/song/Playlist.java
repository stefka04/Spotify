package bg.sofia.uni.fmi.mjt.spotify.song;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Playlist {
    private final String playlistName;
    private Set<Song> songs;

    public Playlist(String playlistName) {
        this.playlistName = playlistName;
        this.songs = new HashSet<>();
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void setSongs(Set<Song> songs) {
        this.songs = songs;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public Set<Song> getSongs() {
        return songs;
    }

    @Override
    public String toString() {
        return  "Playlist " + playlistName + ":" + System.lineSeparator() +
            songs.stream()
                .map(Song::toString)
                .collect(Collectors.joining(System.lineSeparator()));

    }
}
