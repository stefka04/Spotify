package bg.sofia.uni.fmi.mjt.spotify.song;

import javax.sound.sampled.AudioFormat;
import java.io.Serializable;

public class SerializableAudioFormat implements Serializable {

    private SerializableEncoding encoding;

    /**
     * The number of samples played or recorded per second, for sounds that have
     * this format.
     */
    private float sampleRate;

    /**
     * The number of bits in each sample of a sound that has this format.
     */
    private int sampleSizeInBits;

    /**
     * The number of audio channels in this format (1 for mono, 2 for stereo).
     */
    private int channels;

    /**
     * The number of bytes in each frame of a sound that has this format.
     */
    private int frameSize;

    /**
     * The number of frames played or recorded per second, for sounds that have
     * this format.
     */
    private float frameRate;

    /**
     * Indicates whether the audio data is stored in big-endian or little-endian
     * order.
     */
    private boolean bigEndian;

    public SerializableAudioFormat(AudioFormat audioFormat) {
        encoding = new SerializableEncoding(audioFormat.getEncoding().toString());
        sampleRate = audioFormat.getSampleRate();
        sampleSizeInBits = audioFormat.getSampleSizeInBits();
        channels = audioFormat.getChannels();
        frameSize = audioFormat.getFrameSize();
        frameRate = audioFormat.getFrameRate();
        bigEndian = audioFormat.isBigEndian();
    }

    public SerializableEncoding getEncoding() {
        return encoding;
    }

    public void setEncoding(SerializableEncoding encoding) {
        this.encoding = encoding;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    public void setSampleSizeInBits(int sampleSizeInBits) {
        this.sampleSizeInBits = sampleSizeInBits;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }
}
