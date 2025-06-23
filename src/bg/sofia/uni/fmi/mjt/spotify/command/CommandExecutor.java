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

import java.nio.channels.SelectionKey;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.ADD_SONG_TO;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.CREATE_PLAYLIST;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.DISCONNECT;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.LOGIN;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.PLAY;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.REGISTER;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.SHOW_PLAYLIST;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.STOP;
import static bg.sofia.uni.fmi.mjt.spotify.command.CommandType.TOP;

public class CommandExecutor {
    private final Spotify spotify;
    private final ExceptionHandler exceptionHandler;
    private static final int MAX_ARGUMENTS_COUNT = 3;
    private static final int ONE_ARGUMENT = 1;
    private static final int TWO_ARGUMENTS = 2;
    private static final int FIRST_ARGUMENT_INDEX = 0;
    private static final int SECOND_ARGUMENT_INDEX = 1;
    private static final int THIRD_ARGUMENT_INDEX = 2;
    private static final String UNKNOWN_USER = "UNKNOWN USER ";
    private static final String INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT = "Not valid count of arguments: \"%s\"" +
        " expects %d arguments. Example: \"%s\"";
    private static final String ERROR_OCCURRED_MESSAGE = "Unable to connect to the server. Try again later or contact" +
        " administrator by providing the logs in errors.txt";

    public CommandExecutor(Spotify spotify) {
        this.spotify = spotify;
        this.exceptionHandler = new ExceptionHandler();
    }

    public CommandExecutor(Spotify spotify, ExceptionHandler exceptionHandler) {
        this.spotify = spotify;
        this.exceptionHandler = exceptionHandler;
    }

    public String execute(Command command, SelectionKey clientSelectionKey) {
        if (spotify.getIsUserLogged(clientSelectionKey)) {
            String currentUserEmail = spotify.getUsernameOfCurrentUser(clientSelectionKey);
            return switch (command.command()) {
                case DISCONNECT -> disconnect(command.arguments(), clientSelectionKey);
                case SEARCH -> search(command.arguments(), currentUserEmail);
                case TOP -> top(command.arguments(), currentUserEmail);
                case CREATE_PLAYLIST -> createPlaylist(command.arguments(), currentUserEmail);
                case ADD_SONG_TO -> addSongTo(command.arguments(), currentUserEmail);
                case SHOW_PLAYLIST -> showPlaylist(command.arguments(), currentUserEmail);
                case PLAY -> play(command.arguments(), clientSelectionKey, currentUserEmail);
                case STOP -> stopSong(command.arguments(), currentUserEmail);
                case UNKNOWN -> "Unknown command. Please enter valid command";
                default -> "This command is not available when you are logged in. You should disconnect first";
            };
        } else {
            return switch (command.command()) {
                case REGISTER -> register(command.arguments());
                case LOGIN -> login(command.arguments(), clientSelectionKey);
                case DISCONNECT -> disconnect(command.arguments(), clientSelectionKey);
                case UNKNOWN -> "Unknown command. Please enter valid command";
                default -> "You need to login or register to execute this command";
            };
        }
    }

    private String register(String[] commandArguments) {
        if (commandArguments.length != TWO_ARGUMENTS) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, REGISTER, TWO_ARGUMENTS,
                REGISTER + " <email> <password>");
        }

        try {
            spotify.register(commandArguments[FIRST_ARGUMENT_INDEX], commandArguments[SECOND_ARGUMENT_INDEX]);
            return "The registration is successful";
        } catch (UserAlreadyExistsException | WeakPasswordException | CipherException | NoSuchAlgorithmException e) {
            exceptionHandler.handleException(e, commandArguments[FIRST_ARGUMENT_INDEX]);
            return e.getMessage();
        } catch (Exception e) {
            exceptionHandler.handleException(e, commandArguments[FIRST_ARGUMENT_INDEX]);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String login(String[] commandArguments, SelectionKey selectionKey) {
        if (commandArguments.length != TWO_ARGUMENTS) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, LOGIN, TWO_ARGUMENTS,
                LOGIN + " <email> <password>");
        }
        try {
            spotify.login(commandArguments[FIRST_ARGUMENT_INDEX], commandArguments[SECOND_ARGUMENT_INDEX],
                selectionKey);
            return "Login successful";
        } catch (NotFoundUserException | CipherException e) {
            exceptionHandler.handleException(e, commandArguments[FIRST_ARGUMENT_INDEX]);
            return e.getMessage();
        } catch (Exception e) {
            exceptionHandler.handleException(e, commandArguments[FIRST_ARGUMENT_INDEX]);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String disconnect(String[] commandArguments, SelectionKey selectionKey) {
        if (commandArguments.length >= ONE_ARGUMENT) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, DISCONNECT, 0, DISCONNECT);
        }
        String currentUserEmail;
        if (spotify.getIsUserLogged(selectionKey)) {
            currentUserEmail = spotify.getUsernameOfCurrentUser(selectionKey);
        } else {
            currentUserEmail = UNKNOWN_USER;
        }
        try {
            spotify.disconnect(selectionKey);
            return "Disconnect successful";
        } catch (Exception e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String search(String[] commandArguments, String currentUserEmail) {
        if (commandArguments.length == 0) {
            return "Less arguments than needed: expects at least one word. Example: search <words>";
        }
        try {
            Set<String> keywords = Arrays.stream(commandArguments).collect(Collectors.toUnmodifiableSet());
            Set<Song> resultSongs = spotify.search(keywords);
            if (resultSongs.isEmpty()) {
                return "Cannot find the songs you are searching for";
            }
            return resultSongs.stream().map(Song::toString).collect(Collectors.joining(System.lineSeparator()));

        } catch (Exception e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String top(String[] commandArguments, String currentUserEmail) {
        if (commandArguments.length != ONE_ARGUMENT) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, TOP, ONE_ARGUMENT,
                TOP + " <number>");
        }

        try {
            List<Song> topSongs = spotify.top(Integer.parseInt(commandArguments[FIRST_ARGUMENT_INDEX]));
            return topSongs.stream().map(Song::toString).collect(Collectors.joining(System.lineSeparator()));
        } catch (NumberFormatException | IllegalStateException e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return "You should enter only positive number if you want top <number> songs. Example: \"top <3>\"";
        } catch (Exception e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String createPlaylist(String[] commandArguments, String currentUserEmail) {
        if (commandArguments.length != ONE_ARGUMENT) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, CREATE_PLAYLIST, ONE_ARGUMENT,
                CREATE_PLAYLIST + " <name_of_the_playlist>");
        }
        try {
            spotify.createPlaylist(commandArguments[FIRST_ARGUMENT_INDEX]);
            return "Successful creation of the new playlist " + commandArguments[FIRST_ARGUMENT_INDEX];
        } catch (PlaylistAlreadyExistsException e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return e.getMessage();
        } catch (Exception e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String addSongTo(String[] commandArguments, String currentUserEmail) {
        if (commandArguments.length != MAX_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, ADD_SONG_TO, MAX_ARGUMENTS_COUNT,
                ADD_SONG_TO + " <name_of_the_playlist> <song name> <singer name>");
        }
        try {
            spotify.addSongTo(commandArguments[FIRST_ARGUMENT_INDEX], commandArguments[SECOND_ARGUMENT_INDEX],
                commandArguments[THIRD_ARGUMENT_INDEX]);
            return "Song " + commandArguments[SECOND_ARGUMENT_INDEX] + " has been successfully added to " +
                commandArguments[FIRST_ARGUMENT_INDEX];
        } catch (NotFoundPlaylistException | SongAlreadyExistsException | NotFoundSongException e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return e.getMessage();
        } catch (Exception e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String showPlaylist(String[] commandArguments, String currentUserEmail) {
        if (commandArguments.length != ONE_ARGUMENT) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, SHOW_PLAYLIST, ONE_ARGUMENT,
                SHOW_PLAYLIST + " <name_of_the_playlist>");
        }
        try {
            Playlist playlist = spotify.showPlaylist(commandArguments[FIRST_ARGUMENT_INDEX]);
            return playlist.toString();
        } catch (NotFoundPlaylistException e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return e.getMessage();
        } catch (Exception e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String play(String[] commandArguments, SelectionKey selectionKey, String currentUserEmail) {
        if (commandArguments.length != TWO_ARGUMENTS) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, PLAY, TWO_ARGUMENTS,
                PLAY + " <song name> <singer name>");
        }
        try {
            spotify.play(commandArguments[FIRST_ARGUMENT_INDEX], commandArguments[SECOND_ARGUMENT_INDEX], selectionKey);
            return "Streaming: " + commandArguments[FIRST_ARGUMENT_INDEX] + " By " +
                commandArguments[SECOND_ARGUMENT_INDEX];
        } catch (NotFoundSongException e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return e.getMessage();
        } catch (Exception e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return ERROR_OCCURRED_MESSAGE;
        }
    }

    private String stopSong(String[] commandArguments, String currentUserEmail) {
        if (commandArguments.length >= ONE_ARGUMENT) {
            return String.format(INVALID_ARGUMENTS_COUNT_MESSAGE_FORMAT, STOP, 0,
                STOP);
        }
        try {
            spotify.stopSong();
            return "Successfully stop streaming ";
        } catch (Exception e) {
            exceptionHandler.handleException(e, currentUserEmail);
            return ERROR_OCCURRED_MESSAGE;
        }
    }
}
