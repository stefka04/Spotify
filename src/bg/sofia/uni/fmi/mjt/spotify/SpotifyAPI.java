package bg.sofia.uni.fmi.mjt.spotify;

import bg.sofia.uni.fmi.mjt.spotify.exception.CipherException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundSongException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundUserException;
import bg.sofia.uni.fmi.mjt.spotify.exception.PlaylistAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.WeakPasswordException;
import bg.sofia.uni.fmi.mjt.spotify.song.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.song.Song;

import java.nio.channels.SelectionKey;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

public interface SpotifyAPI {
    void register(String email, String password) throws UserAlreadyExistsException,
        WeakPasswordException, CipherException, NoSuchAlgorithmException;

    void login(String email, String password, SelectionKey selectionKey)
        throws NotFoundUserException, CipherException, NoSuchAlgorithmException;

    void disconnect(SelectionKey selectionKey);

    Set<Song> search(Set<String> keywords);

    List<Song> top(int number);

    void createPlaylist(String playlistName) throws PlaylistAlreadyExistsException;

    void addSongTo(String playlistName, String songName, String singerName) throws NotFoundPlaylistException,
        SongAlreadyExistsException, NotFoundSongException;

    Playlist showPlaylist(String playlistName) throws NotFoundPlaylistException;

    void play(String songName, String singerName, SelectionKey selectionKey) throws NotFoundSongException;

    void stopSong();
}
