package com.codepotato.audio_playback;

import android.content.res.*;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.provider.MediaStore;
import android.util.Log;
import com.codepotato.AudioEffects.DelayEffect;
import com.codepotato.AudioEffects.DelayLine;

import java.io.*;
import java.nio.ByteBuffer;


public class Player implements Runnable{
    private boolean isPlaying;
    private int buff_size; //determined at runtime based on hardware, sample rate, channelConfig, audioFormat
    //Activity activity;

    private InputStream is;
    private BufferedInputStream bis;
    private DataInputStream dis;
    private byte[] buff;

    private AudioTrack track;
    private Thread audioThread;
    private boolean isStereo; //stereo or mono
    private static final String LOG_TAG= "XPlayer";

    //delay effect
    private DelayEffect delay;
    private DelayLine delayl;

    /**for the song file that michael has been using, which I assume is stereo
     *
     * @param descriptor
     * @throws IOException
     */
    public Player(AssetFileDescriptor descriptor) throws IOException {
        // setup input stream from given file
        is = new FileInputStream(descriptor.getFileDescriptor());
        //isStereo= true;
        buff_size= AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        //Log.d(LOG_TAG, "Mikes buff_size: " + Integer.toString(buff_size));
        //Log.d(LOG_TAG, descriptor.toString());
        prepare();


    }

    public Player(File audioFile) throws IOException {

        // setup input stream from given file
        is = new FileInputStream(audioFile);
        //audio_format= AudioFormat.CHANNEL_IN_MONO;
        //isStereo= false;
        buff_size= AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        //Log.d(LOG_TAG, "Recorded Audio buff_size: " + Integer.toString(buff_size));

        prepare();

    }

    private void prepare() throws IOException{

        isPlaying = false;
        bis = new BufferedInputStream(is);
        dis = new DataInputStream(bis); //has to do with endian stuff

        // create byte buffer
        buff = new byte[buff_size];

        //setup audio track
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                32000, AudioTrack.MODE_STREAM);

        audioThread= new Thread(this, "Player: Audio Playback Thread");

        // set delay to 1s
        delay = new DelayEffect(88200);
        delay.setDelayTime(44100);

        // set delay parameters
        delay.setWetGain(.8);
        delay.setDryGain(1.);
        delay.setFeedbackGain(.5);
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public void play() {
        audioThread.start(); //executes the code in the Player.run() method
    }

    @Override
    /**
     * This code runs in it's own thread.
     */
    public void run() {
        Log.d("player", "play");

        //set to true
        isPlaying = true;

        //tell track to be ready to play audio
        track.play();

        while(isPlaying){
            try {
                //fill buffer with bytes from file reader
                double sample;
                for(int i=0; i < buff_size; i+= 2)  //increment index by two because 16bit mono sample is 2 bytes long
                {
                    //reads 2 bytes from stream and stores them in buff at offset i. returns number of bytes read.
                    //if bytes are read, condition is TRUE
                    if (bis.read(buff,i, 2) > 0){

                        //bis.read(buff,i, 2);
                        sample = bytesToSample(buff, i);

                        sample = delay.tick(sample);

                        sampleToBytes(sample, buff, i);

                    }
                    else { //EOF: no more bytes to read in bytestream

                        this.pause(); //CAUTION. Might cause a minor bug if pause() just pauses. Might need to define stop();
                        break;

                    }

                }

                //write buffer to track to play
                track.write(buff, 0, buff_size);

            } catch (IOException e) {
                break; //when eof is reached
            }
        }
        //track.pause();
        //track.flush();
    }

    public void pause() {
        Log.d("player", "pause");
        isPlaying = false;
    }

    /**
     * Converts 2 bytes from the buffer(small endian), starting at the offset,
     * into an audio sample of type double (big endian).
     */
    private double bytesToSample(byte[] buff, int offset)
    {
        return ((buff[offset + 0] & 0xFF) | (buff[offset + 1] << 8) ) / 32768.0;
    }

    /**
     * Converts sample of type double into 2 bytes,
     * and stores into the byte buffer starting at the given offset.
     */
    private void sampleToBytes(double sample, byte[] buff, int offset)
    {
        sample = Math.min(1.0, Math.max(-1.0, sample));
        int nsample = (int) Math.round(sample * 32767.0);
        buff[offset + 1] = (byte) ((nsample >> 8) & 0xFF);
        buff[offset + 0] = (byte) (nsample & 0xFF);
    }
}
