package com.codepotato.audio_playback;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import com.codepotato.AudioEffects.ChorusEffect;
import com.codepotato.AudioEffects.EchoEffect;
import com.codepotato.AudioEffects.FlangerEffect;
import com.codepotato.controller.EffectChain;
import com.codepotato.controller.EffectChainFactory;

import java.io.*;


public class Player implements Runnable{
    private boolean isPlaying;
    private int buff_size; //determined at runtime based on hardware, sample rate, channelConfig, audioFormat

    SampleReader sampleReader;
    private byte[] buff;
    EffectChain effectChain;

    private AudioTrack track;
    private Thread audioThread;

    private static final String LOG_TAG= "XPlayer";


    public Player(File audioFile) throws IOException {

        isPlaying = false;
        // setup input stream from given file
        sampleReader = new SampleReader (audioFile, 44100, 16, 1);

        // setup byte buffer
        buff_size = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        buff = new byte[buff_size];

        //setup audio track
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                32000, AudioTrack.MODE_STREAM);

        effectChain = EffectChainFactory.initEffectChain();
        effectChain.addEffect(new FlangerEffect());
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public void play() {
        // create and run new thread for playback
        audioThread= new Thread(this, "Player: Audio Playback Thread");
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

                double sample;
                //fill buffer with bytes from sampleReader
                for(int i=0; i < buff_size; i+= 2)  //increment index by two because 16bit mono sample is 2 bytes long
                {
                    sample = sampleReader.nextSample();

                    sample = effectChain.tickAll(sample);

                    sampleReader.sampleToBytes(sample, buff, i);
                }

                //write buffer to track to play
                track.write(buff, 0, buff_size);

            } catch (IOException e) {
                break; //when eof is reached
            }
        }
    }

    public void pause() {
        Log.d("player", "pause");

        track.pause();
        isPlaying = false;

        // kill playback thread
        audioThread.interrupt();
        audioThread = null;
    }

    public void seekToBeginning() throws IOException {
        sampleReader.seek(0);
        track.flush();
    }


}
