package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.song.SerializableAudioFormat;
import bg.sofia.uni.fmi.mjt.spotify.song.Song;
import com.google.gson.Gson;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MusicServerThread implements Runnable {
    private final SocketChannel clientChannel;
    private final File songFile;
    private final Gson gson;
    private boolean isMusicStopped;
    private static final String END_SONG = "END";
    private static final String MUSIC_PATH = "resources\\music\\";
    private static final String AUDIO_FILE_EXTENSION = ".wav";
    private static final int BUFFER_SIZE = 10000;
    private static final int THREAD_SLEEP_TIME = 20;

    public MusicServerThread(SocketChannel clientChannel, Song song) {
        this.clientChannel = clientChannel;

        this.songFile = new File(MUSIC_PATH + song.getName().replaceAll("\\s", "") + "By" +
            song.getSingerName().replaceAll("\\s", "")
            + AUDIO_FILE_EXTENSION);
        this.gson = new Gson();
        this.isMusicStopped = false;
    }

    @Override
    public void run() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(songFile);
            AudioFormat audioFormat = AudioSystem.getAudioInputStream(songFile).getFormat();
            SerializableAudioFormat serializableAudioFormat = new SerializableAudioFormat(audioFormat);
            String audioFormatJson = gson.toJson(serializableAudioFormat);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            buffer.put(audioFormatJson.getBytes());
            buffer.flip();
            clientChannel.write(buffer);
            byte[] buf = new byte[BUFFER_SIZE];
            while (!isMusicStopped && audioInputStream.read(buf, 0, BUFFER_SIZE) != -1) {
                buffer.clear();
                buffer.put(buf);
                buffer.flip();
                clientChannel.write(buffer);
                Thread.sleep(THREAD_SLEEP_TIME);
            }
            if (!isMusicStopped) {
                isMusicStopped = true;
                buffer.clear();
                buffer.put(END_SONG.getBytes());
                buffer.flip();
                clientChannel.write(buffer);
            }
            audioInputStream.close();
        } catch (UnsupportedAudioFileException | IOException | InterruptedException e) {
            System.out.println("Unable to stop streaming audio");
        }
    }

    public void stopMusic() {
        isMusicStopped = true;
    }
}
