package com.codepotato.FileHandling;

import android.media.AudioFormat;
import android.util.Log;
import com.codepotato.audio_playback.SampleReader;
import com.codepotato.controller.EffectChain;
import com.codepotato.controller.EffectChainFactory;
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

    EffectChain effectChain;

    public FileManager() {
        effectChain= EffectChainFactory.initEffectChain();
    }

    /**
     * Makes copy of a raw audio file in the .wav format. Is placed in the same directory as the raw file.
     *
     * @param rawAudioFile The File object representing the raw file you want to convert
     * @throws java.io.FileNotFoundException
     * @see java.io.File
     */

    public void convertToWavFile(File rawAudioFile)throws IOException{

        int BUFF_SIZE= 10000; //10KB buffer
        //FileInputStream raw_in;
        FileOutputStream wav_out;

        byte data_buffer[] = new byte[BUFF_SIZE];
        int bytesRead = 0;
        int byteCountOffset = 0;

        //remove the .raw extension**** should probably refactor?
        String waveFileNameString= rawAudioFile.getName();
        StringTokenizer stringTokenizer= new StringTokenizer(waveFileNameString, ".");
        waveFileNameString = stringTokenizer.nextToken(); //now we have our audio file without .raw
        waveFileNameString= waveFileNameString.concat(".wav");
        Log.d(LOGTAG, waveFileNameString);
        //---------------

        File wavFile = new File(rawAudioFile.getParent(), waveFileNameString); //TODO-senatori implement a method of deleting the file after it has been shared
        SampleReader sampleReader= new SampleReader(rawAudioFile, SAMPLERATE, 16, 1);
        //raw_in= new FileInputStream(rawAudioFile);
        wav_out= new FileOutputStream(wavFile);

        insertWaveFileHeader(wav_out, rawAudioFile);
        //------------------------
        double sample;
        int bytesProcessed=0;
        while(true){ //Terminates after SampleReader.nextSample() returns 20 consecutive 0.0's
            try {
                int zeroCounter=0; //keeps track of the 0.0 double values returned by nextSample() to determine if were at end of file
                for(bytesProcessed = 0; bytesProcessed < BUFF_SIZE; bytesProcessed+= 2){ //in 16bit mono, a sample is 2 bytes. thus increment by 2
                    sample= sampleReader.nextSample();

                    //TODO-senatori The eof heuristic based code should probably be moved to another function for readability
                    int comparison= Double.compare(0.0, sample); //if sample is equal to 0.0, it could be eof
                    if(comparison == 0){ //so we check that there's been at least 20 consecutive zeros
                        zeroCounter++;
                        Log.d(LOGTAG, Integer.toString(zeroCounter)); //TODO-senatori delete this Log.d statement
                    }
                    else
                        zeroCounter=0; //we want consecutive 0.0's

                    if (zeroCounter == 20){ //EOF (this is a heuristical guess really)
                        break;
                    }
                    //***** add effect chain based code here******

                    //sample = effectChain.tickAll(sample); //run the sample through the effects
                    sampleReader.sampleToBytes(sample, data_buffer, bytesProcessed); //bytesProcessed is the offset

                }
                if (zeroCounter >= 20)
                    break;

                wav_out.write(data_buffer, 0, bytesProcessed);

            }catch(IOException ioe){
                //end of file handling
                break;
            }
        }

        try{
            wav_out.write(data_buffer, 0, bytesProcessed); //write what remains in the buffer upon break/interupt.
            wav_out.close();
            Log.d(LOGTAG, "File Size: " + Long.toString(rawAudioFile.length()));
        }catch(IOException ioe){
            //add useless debug logcat statement here
        }
    }



    /**
     * Inserts the wave header into the ouputstream
     * Wave header was based of Stanford's description of Microsoft's WAVE PCM format at
     * https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
     *
     * @param wave_out The output stream
     * @param rawAudioFile the File object representing the raw audio file
     * @throws java.io.IOException
     */
    private void insertWaveFileHeader(FileOutputStream wave_out, File rawAudioFile) throws IOException {

        long file_size= rawAudioFile.length() + 36; //including the 44 header bytes except for RIFF and File Size header bytes
        long totalAudioLen= rawAudioFile.length(); //the size of just the audio
        int byteRate = BITRATE * SAMPLERATE * NUM_CHANNELS/8; //(bits per sample * Samples per second * channels) / 8 = bytes per second
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (file_size & 0xff);  //converting long to a 16 bit int small endian.
        header[5] = (byte) ((file_size >> 8) & 0xff);
        header[6] = (byte) ((file_size >> 16) & 0xff);
        header[7] = (byte) ((file_size >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // subchunk size(4 bytes). its 16 for PCM. Not sure how it's derived, but this val does NOT represent bitrate
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // Audio Format(2 bytes). PCM = 1. Values other than 1 indicates some form of compression
        header[21] = 0;
        header[22] = (byte) NUM_CHANNELS;
        header[23] = 0;

        //Converting Long int into array of bytes the old fashioned way
        // using & 0xff forces you to just read the 8 bits (in this case the first 8 bits)
        //Also, the endianes is being changed from Big Endian to Little Endian(MS Wave files use little endian)
        header[24] = (byte) (SAMPLERATE & 0xff);//11000000000000011110101 & 11111111 = 11110101 (just the first 8 bits!) remember, 0xFF = 1111 1111
        header[25] = (byte) ((SAMPLERATE >> 8) & 0xff); //shift longint 8 bits to the right to read the next 8 bits,
        header[26] = (byte) ((SAMPLERATE >> 16) & 0xff);//shift longint 16 bits to the right to read the next 8 bits.
        header[27] = (byte) ((SAMPLERATE >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        // block align. Number of bytes an audio frame consists of. Since 16bit mono, a frame consists of one 16 sample, which = 2 bytes
        header[32] = (byte) (NUM_CHANNELS * BITRATE / 8);
        header[33] = 0;
        header[34] = BITRATE;  // bits per sample
        header[35] = 0;
        //data subchunk2
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        wave_out.write(header, 0, 44);

    }

}
