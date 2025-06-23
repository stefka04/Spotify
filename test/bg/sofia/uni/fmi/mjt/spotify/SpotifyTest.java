package bg.sofia.uni.fmi.mjt.spotify;

import bg.sofia.uni.fmi.mjt.spotify.exception.CipherException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundSongException;
import bg.sofia.uni.fmi.mjt.spotify.exception.PlaylistAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.WeakPasswordException;
import bg.sofia.uni.fmi.mjt.spotify.song.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.song.PlaylistService;
import bg.sofia.uni.fmi.mjt.spotify.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SpotifyTest {
    @Mock
    private UserService userServiceMock;
    @Mock
    private PlaylistService playlistServiceMock;
    @InjectMocks
    private Spotify spotify;

    @Test
    void testRegister()
        throws CipherException, UserAlreadyExistsException, NoSuchAlgorithmException, WeakPasswordException {
        doNothing().when(userServiceMock).register("test", "password");

        spotify.register("test", "password");

        verify(userServiceMock).register("test", "password");
    }

    @Test
    void testSearch() {
        Set<Song> expected = Set.of(new Song("test", "test", 0));

        when(playlistServiceMock.searchByKeywords(Set.of("test"))).thenReturn(expected);

        assertIterableEquals(expected, spotify.search(Set.of("test")), "Unexpected output for searching by keywords");
    }

    @Test
    void testTop() {
        List<Song> expected = List.of(new Song("test", "test", 0));

        when(playlistServiceMock.getTopNSongs(1)).thenReturn(expected);

        assertIterableEquals(expected, spotify.top(1), "Unexpected output for top");
    }

    @Test
    void testCreatePlaylist() throws PlaylistAlreadyExistsException {
        doNothing().when(playlistServiceMock).createPlaylist("playlist");

        spotify.createPlaylist("playlist");

        verify(playlistServiceMock).createPlaylist("playlist");
    }

    @Test
    void testAddSongTo() throws NotFoundPlaylistException, SongAlreadyExistsException,
        NotFoundSongException {
        doNothing().when(playlistServiceMock).addSongTo("playlist", "song", "singer");

        spotify.addSongTo("playlist", "song", "singer");

        verify(playlistServiceMock).addSongTo("playlist", "song", "singer");
    }

    @Test
    void testShowPlaylist() throws NotFoundPlaylistException {
        Playlist expected = new Playlist("playlist");
        when(playlistServiceMock.getPlaylistByName("playlist")).thenReturn(expected);

        spotify.showPlaylist("playlist");

        verify(playlistServiceMock).getPlaylistByName("playlist");
    }

    @Test
    void testStop() {
        doNothing().when(playlistServiceMock).stopSong();

        spotify.stopSong();

        verify(playlistServiceMock).stopSong();
    }
}
