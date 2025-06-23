package bg.sofia.uni.fmi.mjt.spotify.command;

import bg.sofia.uni.fmi.mjt.spotify.Spotify;
import bg.sofia.uni.fmi.mjt.spotify.exception.CipherException;
import bg.sofia.uni.fmi.mjt.spotify.exception.ExceptionHandler;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundSongException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundUserException;
import bg.sofia.uni.fmi.mjt.spotify.exception.PlaylistAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.WeakPasswordException;
import bg.sofia.uni.fmi.mjt.spotify.song.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.song.Song;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.channels.SelectionKey;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.ADD_SONG_TO;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.CREATE_PLAYLIST;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.DISCONNECT;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.LOGIN;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.PLAY;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.REGISTER;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.SEARCH;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.SHOW_PLAYLIST;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.STOP;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.TOP;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandExecutorTest {
    @Mock
    private Spotify spotifyMock;
    @Mock
    private ExceptionHandler exceptionHandlerMock;

    @InjectMocks
    private CommandExecutor commandExecutor;

    @Mock
    private SelectionKey selectionKeyMock;

    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_PASSWORD = "testPassword6";
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT = "Not valid count of arguments: \"%s\"" +
            " expects %d arguments. Example: \"%s\"";
    private static final String ERROR_OCCURRED_MESSAGE = "Unable to connect to the server. Try again later or contact" +
        " administrator by providing the logs in errors.txt";

    @Test
    void testRegister()
        throws UserAlreadyExistsException, WeakPasswordException, CipherException, NoSuchAlgorithmException {
        String expected = "The registration is successful";
        String result = commandExecutor.execute(new Command(CommandType.REGISTER, new String[] {USER_EMAIL, USER_PASSWORD}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for register command  Expected:" +  expected + " but was: " + result);

        verify(spotifyMock).register(USER_EMAIL, USER_PASSWORD);
    }

    @Test
    void testRegisterMoreArguments()
        throws UserAlreadyExistsException, WeakPasswordException, CipherException, NoSuchAlgorithmException {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2,
            REGISTER + " <email> <password>");
        String result = commandExecutor.execute(new Command(CommandType.REGISTER, new String[] {USER_EMAIL, USER_PASSWORD, "test"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for register command when there are more arguments than 2");

        verify(spotifyMock, times(0)).register(USER_EMAIL, USER_PASSWORD);
    }

    @Test
    void testRegisterWithWeakPassword()
        throws UserAlreadyExistsException, WeakPasswordException, CipherException, NoSuchAlgorithmException {
        String expected = "Password is weak, should have at least 8 characters, including at least " +
            "one uppercase letter, one lowercase letter and one number";

        doThrow(new WeakPasswordException(expected)).when(spotifyMock).register(USER_EMAIL, USER_PASSWORD);

        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(CommandType.REGISTER, new String[] {USER_EMAIL, USER_PASSWORD}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for register command when there are more arguments than 2");

        verify(spotifyMock).register(USER_EMAIL, USER_PASSWORD);
    }


    @Test
    void testRegisterLessArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2,
            REGISTER + " <email> <password>");

        String result = commandExecutor.execute(new Command(REGISTER, new String[] {USER_EMAIL}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for register command. Expected:" +  expected +
            " but was: " + result);
    }

    @Test
    void testRegisterWhenUserIsLoggedIn() {
        String expected = "This command is not available when you are logged in. You should disconnect first";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        String result = commandExecutor.execute(new Command(REGISTER, new String[] {USER_EMAIL, USER_PASSWORD}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for register. Expected: " + expected +
            " but was " + result);
    }

    @Test
    void testLogin() throws CipherException, NotFoundUserException {
        String expected = "Login successful";
        String result = commandExecutor.execute(new Command(CommandType.LOGIN, new String[] {USER_EMAIL, USER_PASSWORD}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for login command. Expected:" +  expected + " but was: "
            + result);

        verify(spotifyMock).login(USER_EMAIL, USER_PASSWORD, selectionKeyMock);
    }

    @Test
    void testLoginMoreArguments() throws CipherException, NotFoundUserException {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGIN, 2,
            LOGIN + " <email> <password>");
        String result = commandExecutor.execute(new Command(LOGIN, new String[] {USER_EMAIL, USER_PASSWORD, "test"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for login command when there are more arguments than 2");

        verify(spotifyMock, times(0)).login(USER_EMAIL, USER_PASSWORD, selectionKeyMock);
    }

    @Test
    void testLoginWhenUserIsNotFound() throws CipherException, NotFoundUserException {
        String expected = "Unable to login. User with email = " + USER_EMAIL + "is not found. Please try again or register";

        doThrow(new NotFoundUserException(expected)).when(spotifyMock).login(USER_EMAIL, USER_PASSWORD, selectionKeyMock);
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(LOGIN, new String[] {USER_EMAIL, USER_PASSWORD}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for login command. Expected:" +  expected +
            " but was: " + result);
    }

    @Test
    void testDisconnect() {
        String expected = "Disconnect successful";
        String result = commandExecutor.execute(new Command(CommandType.DISCONNECT, new String[] {}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for disconnect command. Expected:" +  expected +
            " but was: " + result);

        verify(spotifyMock).disconnect(selectionKeyMock);
    }

    @Test
    void testDisconnectWhenThereIsAProblemWithRemovingLoggedUser() {
        String expected = ERROR_OCCURRED_MESSAGE;

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        doThrow(new UnsupportedOperationException(expected)).when(spotifyMock).disconnect(selectionKeyMock);
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(CommandType.DISCONNECT, new String[] {}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for disconnect command. Expected:" +  expected +
            " but was: " + result);
    }

    @Test
    void testDisconnectMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, DISCONNECT, 0, DISCONNECT);
        String result = commandExecutor.execute(new Command(DISCONNECT, new String[] {"test"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for disconnect command when expected 0 arguments but was 1");

        verify(spotifyMock, times(0)).disconnect(selectionKeyMock);
    }

    @Test
    void testSearch() {
        Set<String> keywords = Set.of("test");
        Set<Song> expectedSongs = Set.of(new Song("test", "singer", 0));

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        when(spotifyMock.search(keywords)).thenReturn(expectedSongs);

        String expected = "Song: test By singer";
        String result = commandExecutor.execute(new Command(SEARCH, new String[] {"test"}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for search command. Expected: " + expected +
            " but was: " + result);

        verify(spotifyMock).search(keywords);
    }

    @Test
    void testSearchTwoSongsResult() {
        Set<String> keywords = Set.of("test");
        Set<Song> expectedSongs = Set.of(new Song("test", "singer", 0),
        new Song("abc", "artist test", 10));

        String songTest = "Song: test By singer";
        String songAbc = "Song: abc By artist test";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        when(spotifyMock.search(keywords)).thenReturn(expectedSongs);

        String result = commandExecutor.execute(new Command(SEARCH, new String[] {"test"}), selectionKeyMock);
        List<String> tokens = Arrays.stream(result.split(System.lineSeparator())).toList();

        assertTrue(tokens.contains(songTest), "Expected result to contain:" + songTest);
        assertTrue(tokens.contains(songAbc), "Expected result to contain:" + songAbc);
        assertEquals(2, tokens.size(), "Expected result to contain only 2 songs");

        verify(spotifyMock).search(keywords);
    }

    @Test
    void testSearchMoreKeywords() {
        Set<String> keywords = Set.of("TEST", "artist", "dddd");
        Set<Song> expectedSongs = Set.of(new Song("test", "singer", 0),
            new Song("abc", "artist test", 10));
        String songTest = "Song: test By singer";
        String songAbc = "Song: abc By artist test";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        when(spotifyMock.search(keywords)).thenReturn(expectedSongs);

        String result = commandExecutor.execute(new Command(SEARCH, new String[] {"TEST", "artist", "dddd"}),
            selectionKeyMock);
        List<String> tokens = Arrays.stream(result.split(System.lineSeparator())).toList();

        assertTrue(tokens.contains(songTest), "Expected result to contain:" + songTest);
        assertTrue(tokens.contains(songAbc), "Expected result to contain:" + songAbc);
        assertEquals(2, tokens.size(), "Expected result to contain only 2 songs");

        verify(spotifyMock).search(keywords);
    }

    @Test
    void testSearchWithoutKeywords() {
        String expected = "Less arguments than needed: expects at least one word. Example: search <words>";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        String result = commandExecutor.execute(new Command(SEARCH, new String[] {}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for search command when expected at least 1 arguments " +
            "but was 0");

        verify(spotifyMock, times(0)).search(any());
    }

    @Test
    void testTop() {
        List<Song> expectedSongs = List.of(new Song("test", "singer", 2000),
            new Song("abc", "artist", 10));

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        when(spotifyMock.top(2)).thenReturn(expectedSongs);

        String expected = "Song: test By singer" + System.lineSeparator() + "Song: abc By artist";
        String result = commandExecutor.execute(new Command(TOP, new String[] {"2"}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for top command. Expected: " + expected +
            " but was: " + result);

        verify(spotifyMock).top(2);
    }

    @Test
    void testTopNegativeNumber() {
        String expected = "You should enter only positive number if you want top <number> songs. Example: \"top <3>\"";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        when(spotifyMock.top(-1)).thenThrow(new IllegalStateException(expected));
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(TOP, new String[] {"-1"}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for top command. Expected: " + expected +
            " but was: " + result);

        verify(spotifyMock).top(-1);
    }

    @Test
    void testTopWhenArgumentIsNotNumber() {
        String expected = "You should enter only positive number if you want top <number> songs. Example: \"top <3>\"";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        when(spotifyMock.top(-1)).thenThrow(new IllegalStateException(expected));
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(TOP, new String[] {"-1"}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for top command. Expected: " + expected +
            " but was: " + result);

        verify(spotifyMock).top(-1);
    }

    @Test
    void testTopMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, TOP, 1, TOP + " <number>");

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(TOP, new String[] {"5", " songs"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for top command when expected 1 argument but was 2");
    }

    @Test
    void testCreatePlaylist() throws PlaylistAlreadyExistsException {
        String expected = "Successful creation of the new playlist Test";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(CREATE_PLAYLIST, new String[] {"Test"}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for create playlist command. Expected: " + expected +
            " but was: " + result);

        verify(spotifyMock).createPlaylist("Test");
    }

    @Test
    void testCreatePlaylistMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, CREATE_PLAYLIST, 1, CREATE_PLAYLIST +
            " <name_of_the_playlist>");

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(CREATE_PLAYLIST, new String[] {"Test", " songs"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for create playlist command when expected 1 argument but was 2");
    }

    @Test
    void testCreatePlaylistWhenPlaylistAlreadyExists() throws PlaylistAlreadyExistsException {
        String expected = "Unable to create playlist because playlist with this name already exists. " +
            "Please try again and enter different playlist name";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        doThrow(new PlaylistAlreadyExistsException(expected)).when(spotifyMock).createPlaylist("Test");
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(CREATE_PLAYLIST, new String[] {"Test"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for create playlist command. Expected: " + expected +
        " but was: " + result);
    }

    @Test
    void testAddSongTo()
        throws NotFoundPlaylistException, SongAlreadyExistsException, NotFoundSongException {
        String expected = "Song Test has been successfully added to playlist";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(ADD_SONG_TO, new String[] {"playlist", "Test", "artist"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for add song to command. Expected: " + expected +
            " but was: " + result);

        verify(spotifyMock).addSongTo("playlist", "Test", "artist");
    }

    @Test
    void testAddSongToLessArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, ADD_SONG_TO, 3, ADD_SONG_TO +
            " <name_of_the_playlist> <song name> <singer name>");

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(ADD_SONG_TO, new String[] {"Test", " song"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for add song to command when expected 3 arguments but was 2");
    }

    @Test
    void testAddSongToPlaylistNotExist()
        throws NotFoundPlaylistException, SongAlreadyExistsException, NotFoundSongException {

        String expected = "Playlist with name Test does not exist. " +
            "If you want you can create with command create-playlist <name_of_the_playlist>";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        doThrow(new NotFoundPlaylistException(expected)).when(spotifyMock).addSongTo("Test",
            "song", "singer");
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(ADD_SONG_TO, new String[] {"Test", "song", "singer"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for add song to command. Expected: " + expected +
            " but was: " + result);
    }

    @Test
    void testAddSongToSongNotExist()
        throws NotFoundPlaylistException, SongAlreadyExistsException, NotFoundSongException {

        String expected = "Song with name song does not exist";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        doThrow(new NotFoundSongException(expected)).when(spotifyMock).addSongTo("Test",
            "song", "singer");
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(ADD_SONG_TO, new String[] {"Test", "song", "singer"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for add song to command. Expected: " + expected +
            " but was: " + result);
    }

    @Test
    void testAddSongToSongAlreadyExists()
        throws NotFoundPlaylistException, SongAlreadyExistsException, NotFoundSongException {

        String expected = "Song with name song already exist in playlist: Test";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        doThrow(new SongAlreadyExistsException(expected)).when(spotifyMock).addSongTo("Test",
            "song", "singer");
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(ADD_SONG_TO, new String[] {"Test", "song", "singer"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for add song to command. Expected: " + expected +
            " but was: " + result);
    }
    @Test
    void testShowPlaylist() throws NotFoundPlaylistException {
        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        String playlistName = "Test";
        Playlist playlist = new Playlist(playlistName);
        playlist.setSongs(Set.of(new Song("Song1", "Singer1", 0),
            new Song("Song2", "Singer2", 0)));

        String song1 = "Song: Song1 By Singer1";
        String song2 = "Song: Song2 By Singer2";

        when(spotifyMock.showPlaylist(playlistName)).thenReturn(playlist);
        String result = commandExecutor.execute(new Command(SHOW_PLAYLIST, new String[] {playlistName}), selectionKeyMock);

        List<String> tokens = Arrays.stream(result.split(System.lineSeparator())).toList();

        assertEquals("Playlist " + playlistName + ":", tokens.get(0), "Expected result to contain:" + playlistName);
        assertTrue(tokens.contains(song1), "Expected result to contain:" + song1);
        assertTrue(tokens.contains(song2), "Expected result to contain:" + song2);
        assertEquals(3, tokens.size(), "Expected result to contain playlistName and 2 songs");

        verify(spotifyMock).showPlaylist(playlistName);
    }

    @Test
    void testShowPlaylistMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SHOW_PLAYLIST, 1, SHOW_PLAYLIST +
            " <name_of_the_playlist>");

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(SHOW_PLAYLIST, new String[] {"Test", " song"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for show playlist command when expected 1 argument but was 2");
    }

    @Test
    void testShowPlaylistWhenPlaylistNotExist() throws NotFoundPlaylistException {
        String expected = "Playlist with name Test does not exist. " +
            "If you want you can create with command create-playlist <name_of_the_playlist>";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);
        doThrow(new NotFoundPlaylistException(expected)).when(spotifyMock).showPlaylist("Test");
        doNothing().when(exceptionHandlerMock).handleException(any(), any());

        String result = commandExecutor.execute(new Command(SHOW_PLAYLIST, new String[] {"Test"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for show playlist command. Expected: " + expected +
            " but was: " + result);
    }

    @Test
    void testShowPlaylistWhenUserIsNotLogIn() {
        String expected = "You need to login or register to execute this command";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(false);
        String result = commandExecutor.execute(new Command(SHOW_PLAYLIST, new String[] {"name"}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for show playlist. Expected: " + expected +
            " but was " + result);
    }

    @Test
    void testPlayLessArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, PLAY, 2, PLAY +
            " <song name> <singer name>");

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(PLAY, new String[] {"song"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for play command when expected 2 arguments but was 1");
    }

    @Test
    void testPlay() throws NotFoundSongException {
        String expected = "Streaming: song By singer";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(PLAY, new String[] {"song", "singer"}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for play command. Expected: " + expected +
            " but was: " + result);

        verify(spotifyMock).play("song", "singer", selectionKeyMock);
    }

    @Test
    void testStopMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, STOP, 0, STOP);

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(STOP, new String[] {"song"}),
            selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for stop command when expected 0 arguments but was 1");
    }

    @Test
    void testStopSong() {
        String expected = "Successfully stop streaming ";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(STOP, new String[] {}), selectionKeyMock);

        assertEquals(expected, result, "Unexpected output for stop command. Expected: " + expected +
            " but was: " + result);

        verify(spotifyMock).stopSong();
    }

    @Test
    void testExecuteNotValidCommandLoggedUser() {
        String expected = "Unknown command. Please enter valid command";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(true);

        String result = commandExecutor.execute(new Command(UNKNOWN, new String[] {}), selectionKeyMock);

        assertEquals(expected, result, "Not valid command. Expected: " + expected +
            " but was: " + result);
    }

    @Test
    void testExecuteNotValidCommandNotLoggedUser() {
        String expected = "Unknown command. Please enter valid command";

        when(spotifyMock.getIsUserLogged(selectionKeyMock)).thenReturn(false);

        String result = commandExecutor.execute(new Command(UNKNOWN, new String[] {}), selectionKeyMock);

        assertEquals(expected, result, "Not valid command. Expected: " + expected +
            " but was: " + result);
    }
}
