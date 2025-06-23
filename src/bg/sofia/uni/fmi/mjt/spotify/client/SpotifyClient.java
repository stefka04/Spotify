package bg.sofia.uni.fmi.mjt.spotify.client;

import bg.sofia.uni.fmi.mjt.spotify.command.CommandType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SpotifyClient {
    private final int serverPort;
    private MusicClientThread musicThread;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private static ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    public SpotifyClient(int serverPort) {
        this.serverPort = serverPort;
    }

    public static void main(String[] args) {
        final int port = 7777;
        SpotifyClient spotifyClient = new SpotifyClient(port);
        spotifyClient.start();
    }

    public void start() {
        printCommandLineInterface();
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, serverPort));
            while (true) {
                System.out.println("Please enter one of the commands: ");
                String command = scanner.nextLine();
                sendCommandToServer(command, socketChannel);
                if (command.startsWith(CommandType.PLAY.toString())) {
                    musicThread = new MusicClientThread(socketChannel);
                    Thread playMusicThread = new Thread(musicThread);
                    playMusicThread.start();
                } else if (command.startsWith(CommandType.STOP.toString())) {
                    continue;
                }
                String reply = getServerReply(socketChannel);
                System.out.println(reply + System.lineSeparator());
                if (reply.equals("Successfully stop streaming ")) {
                    musicThread.stopMusic();
                }
                if (CommandType.DISCONNECT.toString().equals(command)) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to connect to the server.Try again later or contact administrator " +
                "by providing the logs in <path_to_logs_file> ", e);
        }
    }

    private static void sendCommandToServer(String command, SocketChannel socketChannel) throws IOException {
        buffer.clear();
        buffer.put(command.getBytes());
        buffer.flip();

        socketChannel.write(buffer);
    }

    private static String getServerReply(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    private void printCommandLineInterface() {
        System.out.println("register \"<email>\" \"<password>\"");
        System.out.println("login \"<email>\" \"<password>\"");
        System.out.println("disconnect");
        System.out.println("search \"<words>\"");
        System.out.println("top <number>");
        System.out.println("create-playlist \"<name_of_the_playlist>\"");
        System.out.println("add-song-to \"<name_of_the_playlist>\" \"<song name>\" \"<singer name>\"");
        System.out.println("show-playlist \"<name_of_the_playlist>\"");
        System.out.println("play \"<song name>\" \"<singer name>\"");
        System.out.println("stop");
        System.out.println();
    }
}
