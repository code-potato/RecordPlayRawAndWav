package com.codepotato.FileHandling;

import android.media.AudioFormat;
import android.util.Log;
import com.codepotato.audio_playback.SampleReader;
import com.codepotato.controller.R;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by senatori on 4/20/14.
 */
public class FileManager {
    private static final String LOGTAG = "CodePotatoFileManager";
    //Audio format related variables
    private static final int SAMPLERATE = 44100; //Hz
    private static final int NUM_CHANNELS = 1;
    private static final int BITRATE = 16;

    public FileManager() {
    }

    /**
     * Not Yet finished
     * @param rawAudioFile The File object representing the raw file you want to convert
     * @throws java.io.FileNotFoundException
     * @see java.io.File
     */

    public void convertToWavFile(File rawAudioFile)throws FileNotFoundException{

        int BUFF_SIZE= 10000;
        //FileInputStream raw_in;
        FileOutputStream wav_out;
        int totalAudioLen;
        int totalDataLen;


        //int longSampleRate = SAMPLERATE; //WAV Header info requires Long datatype?


        int byteRate = BITRATE * SAMPLERATE * NUM_CHANNELS/8; //(bits per sample * Samples per second * channels) / 8 = bytes per second
        byte data_buffer[] = new byte[BUFF_SIZE];
        int bytesRead = 0;
        int byteCountOffset = 0;

        //remove the .raw extension**** should probably refactor?
        String waveFileNameString= rawAudioFile.getName();
        StringTokenizer stringTokenizer= new StringTokenizer(waveFileNameString, ".");
        waveFileNameString = stringTokenizer.nextToken(); //now we have our audio file without .raw
        //---------------


        File wavFile = new File(rawAudioFile.getParent(), waveFileNameString);
        SampleReader sampleReader= new SampleReader(rawAudioFile, SAMPLERATE, 16, 1);
        //raw_in= new FileInputStream(rawAudioFile);
        wav_out= new FileOutputStream(wavFile);

        insertWaveFileHeader();
        //------------------------
        double sample;
        int bytesProcessed=0;
        while(true){ //Curently terminates once SampleReader has read the file and throws an excepiton. Sloppy I know...
            try {
                for(bytesProcessed = 0; bytesProcessed < BUFF_SIZE; bytesProcessed+=2){ //in 16bit mono, a sample is 2 bytes. thus increment by 2
                    sample= sampleReader.nextSample();
                    //***** add effect chain based code here******

                    sampleReader.sampleToBytes(sample, data_buffer, bytesProcessed); //bytesProcessed is the offset

                }

                wav_out.write(data_buffer);
            }catch(IOException ioe){
                //end of file handling
                break;
            }
        }
        try{
            wav_out.write(data_buffer, 0, bytesProcessed); //write what remains in the buffer before break/interupt.
            wav_out.close();
        }catch(IOException ioe){
            //add useless debug logcat statement here
        }
    }











    /**
     * Retrieves the dir/path used to store wav files
     * @return
     */
    private File getWavDirectory() {

        File folder= new File(filepath, SAVED_WAV_FOLDER);
        if (!folder.exists())
            folder.mkdir();

        return folder;
    }

    /**
     * NOT YET COMPLETED
     */
    private void insertWaveFileHeader() {

    }



}
