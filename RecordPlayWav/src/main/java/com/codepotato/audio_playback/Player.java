package com.codepotato.audio_playback;

import android.content.res.*;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.provider.MediaStore;
import android.util.Log;
import java.io.*;


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

    /**for the song file that michael has been using, which I assume is stereo
     *
     * @param descriptor
     * @throws IOException
     */
    public Player(AssetFileDescriptor descriptor) throws IOException {
        // setup input stream from given file
        is = new FileInputStream(descriptor.getFileDescriptor());
        isStereo= true;
        buff_size= AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        //Log.d(LOG_TAG, "Mikes buff_size: " + Integer.toString(buff_size));
        //Log.d(LOG_TAG, descriptor.toString());
        prepare();


    }

    public Player(File audioFile) throws IOException {

        // setup input stream from given file
        is = new FileInputStream(audioFile);
        //audio_format= AudioFormat.CHANNEL_IN_MONO;
        isStereo= false;
        buff_size= AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.d(LOG_TAG, "Recorded Audio buff_size: " + Integer.toString(buff_size));


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
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                buff_size, AudioTrack.MODE_STREAM);

        audioThread= new Thread(this, "Player: Audio Playback Thread");
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
                for(int i=0; i < buff_size; i++)
                    buff[i] = dis.readByte();

                // future effect chain goes here

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
