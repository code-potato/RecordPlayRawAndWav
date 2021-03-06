package com.codepotato.controller;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;

import com.codepotato.AudioEffects.ChorusEffect;
import com.codepotato.FileHandling.FileManager;
import com.codepotato.audio_playback.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PlaySound extends Activity {

    private static final String MICHAELS_SOUND_FILE= "emma16.wav"; //Note, that in intelliJ assets dir is under src/main
    private static final String LOG_TAG= "PlayStoundActivity";

    private Player player;
    private File audioFile;

    private boolean is_playing= false;

    FileManager fileManager= new FileManager(); //for testing WAV file conversion and simple Android export



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_sound);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        //------BEEF
        //Retrieving the Intent sent from RecordingMain activity
        Intent intent = getIntent();
        String filepath= intent.getStringExtra("FILEPATH");
        audioFile= new File(filepath);
        try {
            player= new Player(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //------/BEEF

        /***** Test Effect Chain Stuff *****/
//        EffectChain chain = EffectChainFactory.initEffectChain();
//        chain.addEffect(new ChorusEffect());
//        chain.addEffect(new ChorusEffect());
    }

    public void toggleAudio(View view){

        is_playing = !is_playing;
        Button button= (Button)view;

        int button_id= button.getId();


        if(is_playing){ //user clicked on a play button

            //if the Play Recorded Audio button was pressed
            if (button_id == R.id.button){
                updateButtonStatus(button);
//                    player= new Player(audioFile);
                player.play();

            }
            //if the Play Mikes Audio button was pressed
//                else if (button_id == R.id.button2){
//                    updateButtonStatus(button); //first the GUI stuff
//                    // get file from asset folder
//                    AssetFileDescriptor filedes = getAssets().openFd(MICHAELS_SOUND_FILE);
//                    Log.d("FUCK", filedes.toString());
//                    // instantiate player object
//                    player = new Player(filedes);
//                    player.play();
//
//                }
        }

        else{ //if user clicked on a stop button
            updateButtonStatus(button);
            player.pause();

        }

    }

    //Called on a button click
    public void exportWav(View view){
        //TESTING the convert to wav functionality and export
        try {
            File waveFile= fileManager.convertToWavFile(audioFile);
            fileManager.exportToExternalMusicDir(waveFile, this.getApplicationContext());
            File fileArray[] = fileManager.listRawFiles(this.getApplicationContext());

            /*for (File file_iterator: fileArray)
                fileManager.deleteFile(file_iterator);*/


        } catch (IOException e) {
            Log.d(LOG_TAG,"Some IO error occured in my exportWav funciton!", e);
        }
    }

    public void restartPlayback(View view){
        try {
            player.seekToBeginning();
            Log.d("seekToBeginning", "succeeded");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("seekToBeginning", "failed");
        }
    }

    /**
     * Determines which button has been pressed and toggles button text and visibility
     * @param pressedButton pass the button that has been pressed
     */
    private void updateButtonStatus(Button pressedButton) {

        int button_id= pressedButton.getId();
        Button notSelectedButton;//first the GUI stuff
        if(is_playing){ //a Play button has been pressed. Make respective GUI changes to the buttons.
            if(button_id == R.id.button){
                pressedButton.setText(this.getString(R.string.RecordMain_stop_recorded_audio)); //gets & sets string value stored in res/values/strings.xml
                //hide visiblity of other button so no two songs get played simultaneously
//                notSelectedButton= (Button) findViewById(R.id.button2);
//                notSelectedButton.setVisibility(View.INVISIBLE);
            }
            else{
                pressedButton.setText(this.getString(R.string.RecordMain_stop_michaels_audio));
                //hide visibility of the other button
                notSelectedButton= (Button) findViewById(R.id.button);
                notSelectedButton.setVisibility(View.INVISIBLE);
            }
        }
        else{//a Stop button has been pressed. Make Respective GUI changes to the buttons.
            if(button_id == R.id.button){
                pressedButton.setText(this.getString(R.string.RecordMain_play_recorded_audio)); //gets & sets string value stored in res/values/strings.xml
                //hide visiblity of other button so no two songs get played simultaneously
//                notSelectedButton= (Button) findViewById(R.id.button2);
//                notSelectedButton.setVisibility(View.VISIBLE);
            }
            else{
                pressedButton.setText(this.getString(R.string.RecordMain_play_michaels_audio));
                //hide visibility of the other button
                notSelectedButton= (Button) findViewById(R.id.button);
                notSelectedButton.setVisibility(View.VISIBLE);
            }
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.play_sound, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_play_sound, container, false);
            return rootView;
        }
    }

}
