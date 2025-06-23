package bg.sofia.uni.fmi.mjt.spotify.song;

import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundSongException;
import bg.sofia.uni.fmi.mjt.spotify.exception.PlaylistAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.SongAlreadyExistsException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaylistServiceTest {
    private static PlaylistService playlistService;
    private static final String SAMPLE_SONGS = """
        test-Song Singer-Unknown 204
        Random Test-Singer 164""" + System.lineSeparator();

    @BeforeAll
    static void setUpTestCase() {
        playlistService = new PlaylistService(new StringReader(SAMPLE_SONGS),
            Set.of(new Playlist("Playlist"), new Playlist("PlaylistTest")));
    }

    @Test
    void testSearchByKeywords() {
        Song testSong = new Song("test Song", "Singer Unknown", 0);
        Song testSinger =  new Song("Random", "Test Singer", 0);

        Set<Song> expected = Set.of(testSong, testSinger);
        Set<Song> result = playlistService.searchByKeywords(Set.of("test"));

        assertEquals(expected.size(), result.size(), "Expected count of songs: " + expected.size() + " but was: "
            + result.size());
        assertTrue(result.contains(testSong), "Result should contain " + testSong);
        assertTrue(result.contains(testSinger),"Result should contain " + testSinger);
    }

    @Test
    void testSearchByKeywordsOneSong() {
        Song testSinger =  new Song("Random", "Test Singer", 0);

        Set<Song> expected = Set.of(testSinger);
        Set<Song> result = playlistService.searchByKeywords(Set.of("Random"));

        assertEquals(expected.size(), result.size(), "Expected count of songs: " + expected.size() +
            " but was: " + result.size());
        assertTrue(result.contains(testSinger),"Result should contain " + testSinger);
    }

    @Test
    void testGetTopNSongs() {
        Song testSong = new Song("test Song", "Singer Unknown", 204);
        Song testSinger =  new Song("Random", "Test Singer", 164);

        List<Song> expected = List.of(testSong, testSinger);
        List<Song> result = playlistService.getTopNSongs(2);

        assertIterableEquals(expected, result, "Expected: " + expected + " but was: " + result);
    }

    @Test
    void testGetTopNSongsNegativeN() {
        assertThrows(IllegalStateException.class, () -> playlistService.getTopNSongs(-6),
            "IllegalStateException expected when number is negative but nothing was thrown");
    }

    @Test
    void testGetPlaylistByNameWhenPlaylistNotExist() {
        assertThrows(NotFoundPlaylistException.class, () -> playlistService.getPlaylistByName("not found"),
            "NotFoundPlaylistException expected when playlist not exist but nothing was thrown");
    }

    @Test
    void testGetPlaylist() throws NotFoundPlaylistException {
        Playlist expected = new Playlist("Playlist");
        PlaylistService playlistServiceTest = new PlaylistService(new StringReader(SAMPLE_SONGS), Set.of(expected));
        Playlist result = playlistServiceTest.getPlaylistByName("Playlist");

        assertEquals(expected, result, "Expected: " + expected + " but was: " + result);
    }

    @Test
    void testCreatePlaylistWhenPlaylistExist() {
        assertThrows(PlaylistAlreadyExistsException.class, () -> playlistService.createPlaylist("Playlist"),
            "PlaylistAlreadyExistsException expected when playlist already exist but nothing was thrown");
    }

    @Test
    void testAddSongToPlaylistWhenPlaylistNotExist() {
        assertThrows(NotFoundPlaylistException.class, () -> playlistService.addSongTo("not found",
                "song", "singer"),
            "NotFoundPlaylistException expected when playlist not exist but nothing was thrown");
    }

    @Test
    void testAddSongToPlaylistWhenSongNotExist() {
        Playlist expected = new Playlist("Playlist");
        expected.setSongs(Set.of(new Song("song", "singer", 10)));
        PlaylistService playlistServiceTest = new PlaylistService(new StringReader(SAMPLE_SONGS), Set.of(expected));

        assertThrows(NotFoundSongException.class, () -> playlistServiceTest.addSongTo("Playlist",
                "test", "singer"),
            "NotFoundSongException expected when song not exist but nothing was thrown");
    }

    @Test
    void testAddSongToPlaylistWhenSongAlreadyExist() {
        Playlist expected = new Playlist("Playlist");
        expected.setSongs(Set.of(new Song("Random", "Test Singer", 10)));
        PlaylistService playlistServiceTest = new PlaylistService(new StringReader(SAMPLE_SONGS), Set.of(expected));

        assertThrows(SongAlreadyExistsException.class, () -> playlistServiceTest.addSongTo("Playlist",
                "Random", "Test Singer"),
            "SongAlreadyExistsException expected when song not exist but nothing was thrown");
    }
}
