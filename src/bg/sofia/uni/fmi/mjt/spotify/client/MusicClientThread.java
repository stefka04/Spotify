package bg.sofia.uni.fmi.mjt.spotify.client;

import bg.sofia.uni.fmi.mjt.spotify.song.SerializableAudioFormat;
import com.google.gson.Gson;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MusicClientThread implements Runnable {
    private final SocketChannel clientChannel;
    private final Gson gson;
    private static final int BUFFER_SIZE = 10004;
    private boolean isMusicStopped = false;

    public MusicClientThread(SocketChannel clientChannel) {
        this.clientChannel = clientChannel;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            String audioFormatJson = getAudioFormatJson(buffer);
            SerializableAudioFormat serializableAudioFormat = gson.fromJson(audioFormatJson,
                SerializableAudioFormat.class);
            DataLine.Info info = getInfo(serializableAudioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open();
            sourceDataLine.start();
            byte[] byteArray = new byte[BUFFER_SIZE];
            int readBytes = 0;
            buffer.clear();
            while (!isMusicStopped && (readBytes = clientChannel.read(buffer)) != -1) {
                buffer.flip();
                buffer.get(byteArray, 0, readBytes);
                String checkMessage = new String(byteArray);
                if (checkMessage.contains("END") || checkMessage.contains("Successfully stop streaming ")) {
                    break;
                }
                sourceDataLine.write(byteArray, 0, readBytes);
                buffer.clear();
            }
            isMusicStopped = true;
            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (IOException | LineUnavailableException e) {
            System.out.println("A problem occurred while streaming audio " + e.getMessage());
        }
    }

    private String getAudioFormatJson(ByteBuffer buffer) throws IOException {
        clientChannel.read(buffer);
        buffer.flip();

        byte[] audioFormatBytes = new byte[buffer.remaining()];
        buffer.get(audioFormatBytes);
        return new String(audioFormatBytes);
    }

    private DataLine.Info getInfo(SerializableAudioFormat serializableAudioFormat) {
        AudioFormat audioFormat = new AudioFormat(new AudioFormat.Encoding(serializableAudioFormat
            .getEncoding().getName()), serializableAudioFormat.getSampleRate(),
            serializableAudioFormat.getSampleSizeInBits(), serializableAudioFormat.getChannels(),
            serializableAudioFormat.getFrameSize(), serializableAudioFormat.getFrameRate(),
            serializableAudioFormat.isBigEndian());
        return new DataLine.Info(SourceDataLine.class, audioFormat);
    }

    public void stopMusic() {
        isMusicStopped = true;
    }
}
