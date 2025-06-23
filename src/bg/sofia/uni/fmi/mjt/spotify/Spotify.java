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
import bg.sofia.uni.fmi.mjt.spotify.song.PlaylistService;
import bg.sofia.uni.fmi.mjt.spotify.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.user.UserService;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Spotify implements SpotifyAPI {
    private final UserService userService;
    private final PlaylistService playlistService;
    private final Set<SelectionKey> loggedUsers;

    public Spotify() {
        userService = new UserService();
        playlistService = new PlaylistService();
        loggedUsers = new HashSet<>();
    }

    public Spotify(UserService userService, PlaylistService playlistService) {
        this.userService = userService;
        this.playlistService = playlistService;
        loggedUsers = new HashSet<>();
    }

    @Override
    public void register(String email, String password) throws UserAlreadyExistsException, WeakPasswordException,
        CipherException, NoSuchAlgorithmException {

        userService.register(email, password);
    }

    @Override
    public void login(String email, String password, SelectionKey selectionKey) throws NotFoundUserException,
        CipherException {
        if (loggedUsers.contains(selectionKey)) {
            return;
        }

        userService.login(email, password);
        selectionKey.attach(email);
        loggedUsers.add(selectionKey);
    }

    @Override
    public void disconnect(SelectionKey selectionKey) {
        loggedUsers.remove(selectionKey);
    }

    @Override
    public Set<Song> search(Set<String> keywords) {
        return playlistService.searchByKeywords(keywords);
    }

    @Override
    public List<Song> top(int number) {
        return playlistService.getTopNSongs(number);
    }

    @Override
    public void createPlaylist(String playlistName)
        throws PlaylistAlreadyExistsException {

        playlistService.createPlaylist(playlistName);
    }

    @Override
    public void addSongTo(String playlistName, String songName, String singerName)
        throws NotFoundPlaylistException, SongAlreadyExistsException, NotFoundSongException {

        playlistService.addSongTo(playlistName, songName, singerName);
    }

    @Override
    public Playlist showPlaylist(String playlistName)
        throws NotFoundPlaylistException {

        return playlistService.getPlaylistByName(playlistName);
    }

    @Override
    public void play(String songName, String singerName, SelectionKey selectionKey)
        throws NotFoundSongException {

        playlistService.playSong(songName, singerName, (SocketChannel) selectionKey.channel());

    }

    @Override
    public void stopSong() {
        playlistService.stopSong();
    }

    public String getUsernameOfCurrentUser(SelectionKey selectionKey) {
        return loggedUsers.stream()
            .filter(userKey -> userKey.equals(selectionKey))
            .findFirst()
            .get()
            .attachment()
            .toString();
    }

    public boolean getIsUserLogged(SelectionKey selectionKey) {
        return loggedUsers.contains(selectionKey);
    }
}