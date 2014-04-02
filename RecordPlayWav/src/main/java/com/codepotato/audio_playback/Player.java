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

        //make buff_size divisible by 8
        buff_size += 8 - buff_size%8;

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

        //make buff_size divisible by 8
        buff_size += 8 - buff_size%8;


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

        //delay line testing
        delayl = new DelayLine(88200);
        delayl.setDelayLineDelay(44100);

        // set delay to 100ms
        delay = new DelayEffect(8820);
        delay.setDelayTime(4410);

        // set delay parameters
        delay.setWetGain(1);
        delay.setDryGain(1);
        delay.setFeedbackGain(0);
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

        /*try {
            dis.skipBytes(44);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ByteBuffer bb = ByteBuffer.allocate(8);
        while(isPlaying){
            try {
                //fill buffer with bytes from file reader
                for(int i=0; i < buff_size/8; i++)
                {
                    //buff[i] = dis.readByte();

                    /*
                    Read double from input, tick() the effects,
                    then save to bytebuffer.
                     */
                    bb.putDouble(0, delayl.tick(dis.readDouble()));
                    bb.rewind();
                    bb.get(buff,i*8,8);
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

}
