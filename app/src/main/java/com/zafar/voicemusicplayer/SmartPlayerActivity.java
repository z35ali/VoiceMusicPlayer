package com.zafar.voicemusicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class SmartPlayerActivity extends AppCompatActivity {

    private RelativeLayout parentRelativeLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private ImageView pausePlayBtn, nextBtn, previousBtn;
    private TextView songNameText;
    private ImageView imageView;
    private RelativeLayout lowerLayout;
    private Button voiceEnableBtn;
    private boolean voiceMode = true;

    private MediaPlayer mediaPlayer;
    private int position;
    private ArrayList<File> songs;
    private String songName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_player);

        checkVoiceCommandPermission();
        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);
        pausePlayBtn = findViewById(R.id.pause_play_btn);
        nextBtn = findViewById(R.id.next_btn);
        previousBtn = findViewById(R.id.previous_btn);
        imageView = findViewById(R.id.logo);
        lowerLayout = findViewById(R.id.lower);
        voiceEnableBtn = findViewById(R.id.voice_enable_btn);
        songNameText = findViewById(R.id.songName);


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(SmartPlayerActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        validateReceiveStart();
        imageView.setBackgroundResource(R.drawable.four);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

                ArrayList<String> matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matchesFound != null){
                    keeper = matchesFound.get(0);

                    if (keeper.equals("pause") || keeper.equals("pause the song") || keeper.equals("pause song")){
                        playPause();
                        Toast.makeText(SmartPlayerActivity.this, "Command: "+ keeper, Toast.LENGTH_LONG).show();
                    }else if (keeper.equals("play") || keeper.equals("play the song") || keeper.equals("play song")){
                        playPause();
                        Toast.makeText(SmartPlayerActivity.this, "Command: "+ keeper, Toast.LENGTH_LONG).show();
                    }

                    Toast.makeText(SmartPlayerActivity.this, "Result = " + keeper, Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
        parentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        keeper = "";
                    break;

                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;

                }
                return false;
            }
        });

        voiceEnableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (voiceMode){
                    voiceEnableBtn.setText("Voice Enabled - OFF");
                    voiceMode = false;
                    lowerLayout.setVisibility(View.VISIBLE);
                }else{
                    voiceEnableBtn.setText("Voice Enabled - ON");
                    lowerLayout.setVisibility(View.GONE);
                    voiceMode = true;

                }
            }
        });

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });
    }

    private void validateReceiveStart(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        position = bundle.getInt("position", 0);
        songs= (ArrayList) bundle.getParcelableArrayList("song");
        songName = songs.get(position).getName();
        String mSongName = intent.getStringExtra("name");

        songNameText.setText(songName);
        songNameText.setSelected(true);


        Uri uri = Uri.parse(songs.get(position).toString());

        mediaPlayer = MediaPlayer.create(SmartPlayerActivity.this, uri);
        mediaPlayer.start();

    }

    private void checkVoiceCommandPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (!(ContextCompat.checkSelfPermission(SmartPlayerActivity.this,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)){

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    private void playPause(){
        if (mediaPlayer.isPlaying()){
            imageView.setBackgroundResource(R.drawable.four);
            pausePlayBtn.setImageResource(R.drawable.play);
            mediaPlayer.pause();

        }else{
            pausePlayBtn.setImageResource(R.drawable.pause);
            mediaPlayer.start();
            imageView.setBackgroundResource(R.drawable.five);

        }
    }
}
