package bg.sofia.uni.fmi.mjt.spotify.song;

import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.exception.NotFoundSongException;
import bg.sofia.uni.fmi.mjt.spotify.exception.PlaylistAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.exception.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.server.MusicServerThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.SocketChannel;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaylistService {
    public static final String SEPARATOR = "-";
    private final Set<Playlist> playlists;
    private final Set<Song> allSongs;
    private  MusicServerThread musicServerThread;

    private static final String SONGS_FILE_PATH = "resources\\AllSongs.txt";
    private static final String PLAYLISTS_FILE_PATH = "resources\\playlists\\";
    private static final String SPACE = " ";
    private static final String TEXT_FILE_EXTENSION = ".txt";
    private static final int SONG_NAME_INDEX = 0;
    private static final int SINGER_NAME_INDEX = 1;
    private static final int PLAYING_TIMES_INDEX = 2;

    public PlaylistService() {
        playlists = readPlaylistsFromFile();
        try {
            allSongs = readSongsFromFile(new FileReader(SONGS_FILE_PATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public PlaylistService(Reader playlistsReader, Set<Playlist> playlists) {
        this.playlists = playlists;
        this.allSongs = readSongsFromFile(playlistsReader);
    }

    public Set<Song> searchByKeywords(Set<String> keywords) {
        Set<String> searchableKeywords = keywords.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toUnmodifiableSet());

        return allSongs.stream()
            .filter(song -> Stream.concat(Arrays.stream(song.getName().split(SPACE)),
                    Arrays.stream(song.getSingerName().split(SPACE)))
                .map(String::toLowerCase)
                .anyMatch(searchableKeywords::contains))
            .collect(Collectors.toUnmodifiableSet());
    }

    public List<Song> getTopNSongs(int number) {
        if (number <= 0) {
            throw new IllegalStateException("Unable to get top songs because the provided number is negative." +
                " Please try again and enter positive number");
        }

        return allSongs.stream()
            .sorted(Comparator.comparing(Song::getPlayingTimes).reversed())
            .limit(number)
            .toList();
    }

    public void createPlaylist(String playlistName) throws PlaylistAlreadyExistsException {
        if (checkIfPlaylistNameAlreadyExists(playlistName)) {
            throw new PlaylistAlreadyExistsException("Unable to create playlist because playlist with this name " +
                "already exists. Please try again and enter different playlist name");
        }
        playlists.add(new Playlist(playlistName));
        createPlaylistFile(playlistName);
    }

    public void addSongTo(String playlistName, String songName, String singerName)
        throws NotFoundPlaylistException, NotFoundSongException, SongAlreadyExistsException {
        Playlist playlist = getPlaylistByName(playlistName);
        Song song = getSong(songName, singerName);
        if (playlist.getSongs().contains(song)) {
            throw new SongAlreadyExistsException("Unable to add song " + " because playlist with this name " +
                "already exists. Please try again and enter different playlist name");
        }
        playlist.addSong(song);
        String fileName = PLAYLISTS_FILE_PATH + playlistName.replaceAll("\\s", SEPARATOR) + TEXT_FILE_EXTENSION;
        addSongToPlaylistFile(song, fileName);
    }

    public Playlist getPlaylistByName(String playlistName) throws NotFoundPlaylistException {
        return playlists.stream()
            .filter(playlist -> playlist.getPlaylistName().equals(playlistName))
            .findFirst()
            .orElseThrow(() -> new NotFoundPlaylistException("Playlist with name " + playlistName + " does not exist." +
                " If you want you can create with command create-playlist <name_of_the_playlist>"));
    }

    public void playSong(String songName, String singerName, SocketChannel clientChannel) throws NotFoundSongException {
        Song wantedSong = getSong(songName, singerName);
        wantedSong.increasePlayingTimes();
        updateAllSongsFile();

        musicServerThread = new MusicServerThread(clientChannel, wantedSong);
        Thread musicThread = new Thread(musicServerThread);
        musicThread.start();
    }

    public void stopSong() {
        musicServerThread.stopMusic();
    }

    public void updateAllSongsFile() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(SONGS_FILE_PATH))) {
            for (Song song : allSongs) {
                bufferedWriter.write(song.getName().replaceAll("\\s", SEPARATOR) + SPACE +
                    song.getSingerName().replaceAll("\\s", SEPARATOR) + SPACE +
                    song.getPlayingTimes() + System.lineSeparator());
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }

    private Set<Song> readSongsFromFile(Reader songsReader) {
        Set<Song> allSongs = new HashSet<>();
        try (BufferedReader bufferedReader = new BufferedReader(songsReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals(System.lineSeparator())) {
                    continue;
                }
                String[] tokens = line.split(SPACE);
                String songName = String.join(SPACE, tokens[SONG_NAME_INDEX].split(SEPARATOR));
                String singerName = String.join(SPACE, tokens[SINGER_NAME_INDEX].split(SEPARATOR));

                allSongs.add(new Song(songName, singerName, Integer.parseInt(tokens[PLAYING_TIMES_INDEX])));
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from a file", e);
        }
        return allSongs;
    }

    private Set<Playlist> readPlaylistsFromFile() {
        Set<Playlist> playlists = new HashSet<>();
        Path dir = Path.of(PLAYLISTS_FILE_PATH);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path playlistFile : stream) {
                String fileName = playlistFile.getFileName().toString();
                String withoutFileExtension = fileName.substring(0, fileName.length() - TEXT_FILE_EXTENSION.length());
                String playlistName = String.join(SPACE, withoutFileExtension.split(SEPARATOR));
                Playlist playlist = new Playlist(playlistName);
                Set<Song> songs = new HashSet<>();
                try (BufferedReader bufferedReader = Files.newBufferedReader(playlistFile)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.equals(System.lineSeparator())) {
                            continue;
                        }
                        String[] tokens = line.split(SPACE);
                        String songName = String.join(SPACE, tokens[SONG_NAME_INDEX].split(SEPARATOR));
                        String singerName = String.join(SPACE, tokens[SINGER_NAME_INDEX].split(SEPARATOR));
                        songs.add(new Song(songName, singerName, 0));
                    }
                }
                playlist.setSongs(songs);
                playlists.add(playlist);
            }
        } catch (IOException | DirectoryIteratorException e) {
            throw new IllegalStateException("A problem occurred while reading from a file", e);
        }
        return playlists;
    }

    private void addSongToPlaylistFile(Song song, String filePath) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, true))) {
            bufferedWriter.write(song.getName().replaceAll("\\s", SEPARATOR) + SPACE +
                song.getSingerName().replaceAll("\\s", SEPARATOR) + System.lineSeparator());
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }

    private void createPlaylistFile(String playlistName) {
        String fileName = PLAYLISTS_FILE_PATH + playlistName.replaceAll("\\s", SEPARATOR) + TEXT_FILE_EXTENSION;
        Path playlistFile = Paths.get(fileName);
        try {
            Files.createFile(playlistFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkIfPlaylistNameAlreadyExists(String playlistName) {
        return playlists.stream()
            .anyMatch(playlist -> playlist.getPlaylistName().equals(playlistName));
    }

    private Song getSong(String songName, String singerName) throws NotFoundSongException {
        return allSongs.stream()
            .filter(currentSong -> currentSong.getName().equalsIgnoreCase(songName) &&
                currentSong.getSingerName().equalsIgnoreCase(singerName))
            .findFirst().orElseThrow(() -> new NotFoundSongException("Song " + songName + " by: " + singerName +
                " is not available"));
    }
}
