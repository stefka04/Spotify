package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.Spotify;
import bg.sofia.uni.fmi.mjt.spotify.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.spotify.command.CommandExecutor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class SpotifyServer {
    private static final int BUFFER_SIZE = 1024;
    private static final String SERVER_HOST = "localhost";
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private Selector selector;
    private boolean isServerRunning;
    private final int serverPort;
    private final CommandExecutor executor;

    public SpotifyServer(int serverPort, CommandExecutor commandExecutor) {
        this.serverPort = serverPort;
        this.executor = commandExecutor;
    }

    public static void main(String[] args) {
        final int port = 7777;
        CommandExecutor commandExecutor = new CommandExecutor(new Spotify());
        SpotifyServer server = new SpotifyServer(port, commandExecutor);
        server.start();
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel);
            isServerRunning = true;
            while (isServerRunning) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        String clientInput = getClientInput(clientChannel);
                        if (clientInput == null) {
                            continue;
                        }
                        writeClientOutput(clientChannel, executor.execute(CommandCreator.newCommand(clientInput), key));
                    } else if (key.isAcceptable()) {
                        acceptNewConnection(selector, key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("There is a problem with the server socket", e);
        }
    }

    public void stop() {
        isServerRunning = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel serverSocketChannel) throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, serverPort));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            System.out.println("Client has closed the connection");
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);
        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void acceptNewConnection(Selector selector, SelectionKey selectionKey) throws IOException {
        ServerSocketChannel socketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel accept = socketChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }
}
