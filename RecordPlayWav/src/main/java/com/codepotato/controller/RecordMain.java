package com.codepotato.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.codepotato.audio_recording.Recorder;

import java.io.File;

public class RecordMain extends Activity {

    public static final String LOG_TAG= "CodePotatoAudioRecordingTest"; //for debugging purposes
    private Recorder recorder;

    private TextView textTimer;
    private long startTime = 0L;
    private Handler myHandler = new Handler();
    long elapsedTime = 0L;


    /**
     * This function is called upon a The Record button press in the main view. This is the insertion point
     * @param view is passed implicitly by the GUI.
     */
    public void toggleRecording(View view){
        ToggleButton recordToggle= (ToggleButton) view;


        //Start Recording is pressed
        if(recordToggle.isChecked()){
            recordToggle.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.done_button)); //changes the buttons background image
            startRecording();

        }
        //Stop Recording is pressed
        else{
            recordToggle.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.record_button));//changes the button background image
            stopRecording();

        }


        //context.getString(R.string.some_text);
    }

    public void startRecording(){

        File filepath= this.getFilesDir();  //returns us the root of the apps private sandboxed directory
        recorder= new Recorder(filepath);
        recorder.start();

        //Starts the Stopwatch/Timer
        elapsedTime = 0L;
        startTime = SystemClock.uptimeMillis();
        myHandler.postDelayed(updateTimer, 1000);


    }

    public void stopRecording(){
        recorder.stop();

        myHandler.removeCallbacks(updateTimer); //stops the timer

        promptUserForSaveFileName();//prompts user for File Name via an Alert Dialogue box.

    }

    // A stopwatch thread for the audio recording.
    private Runnable updateTimer = new Runnable() {

        public void run() {
            elapsedTime = SystemClock.uptimeMillis() - startTime;
            int seconds = (int) (elapsedTime / 1000);
            seconds = seconds % 60;
            int minutes = seconds / 60;
            String minutesPrefix = "";
            if (minutes < 10)
                minutesPrefix = "0";

            textTimer.setText(minutesPrefix + minutes + ":"
                    + String.format("%02d", seconds));
            myHandler.postDelayed(this, 1000);
        }
    };

    private void promptUserForSaveFileName() {

        //EditText value;
        // get activity_initial_scr_prompt.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.activity_initial_scr_prompt, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this); //
        alert.setTitle("Enter File Name:");
        alert.setView(promptView);
        final EditText input = (EditText) promptView.findViewById(R.id.userInput);
        alert.setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {  //IF THE USER CLICKED ON SAVE BUTTON
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        Log.d(LOG_TAG, "The Value is: " + value.toString());
                        File namedAudioFile = recorder.save(value.toString()); //TODO-senatori refactor this to somewhere more intuitive
                        textTimer.setText("00:00");
                        prepareToSwitchViews(namedAudioFile.toString()); //a method defined in this activity.

                        //Intent intent = new Intent(RecordMain.this, /*EffectsConfigScr.class*/ PlaySound.class);
                        //intent.putExtra("FILEPATH", namedAudioFile.toString());//a hash...read bellow
                        /* An Intent can carry a payload of various data types as key-value pairs called extras.
                         *  The putExtra() method takes the key name in the first arg and the value in the second arg
                         */
                        //startActivity(intent);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        dialog.cancel();
                        textTimer.setText("00:00");
                    }
                });
        alert.show();

        //Log.d(LOG_TAG, "The Value in EditText is: " + value.toString());
        //return value.toString();
    }

    /** switches to a different view/activity after recording has finished     */
    private void prepareToSwitchViews(String filepath) {

        //In order to switch Activity/view, you must use an Intent
        Intent intent = new Intent(this, PlaySound.class);//this is the current context, PlaySound.class is the activity we want to switch to

        intent.putExtra("FILEPATH", filepath);//a hash...read bellow
        /* An Intent can carry a payload of various data types as key-value pairs called extras.
        The putExtra() method takes the key name in the first arg and the value in the second arg
         */
        startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        textTimer = (TextView) findViewById(R.id.stopwatch);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_main, menu);
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
            View rootView = inflater.inflate(R.layout.fragment_record_main, container, false);
            return rootView;
        }
    }

}
