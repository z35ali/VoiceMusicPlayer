package com.zafar.voicemusicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
    private TextView startTime;
    private TextView endTime;
    private ImageView imageView;
    private RelativeLayout lowerLayout;
    private Button voiceEnableBtn;
    SeekBar seekBar;
    private boolean voiceMode;

    private MediaPlayer mediaPlayer;
    private int position;
    private ArrayList<File> songs;
    private String songName;

    private long backPressedTime;
    Handler handler;
    Runnable runnable;

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
        seekBar = findViewById(R.id.seekBar);
        voiceMode = false;
        lowerLayout.setVisibility(View.VISIBLE);


        handler = new Handler();

        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);






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

                    if (voiceMode){
                        keeper = matchesFound.get(0);

                        if (keeper.equals("pause") || keeper.equals("pause the song") || keeper.equals("pause song")){
                            pause();
                            Toast.makeText(SmartPlayerActivity.this, "Command: "+ keeper, Toast.LENGTH_LONG).show();
                        }else if (keeper.equals("play") || keeper.equals("play the song") || keeper.equals("play song")){
                            play();
                            Toast.makeText(SmartPlayerActivity.this, "Command: "+ keeper, Toast.LENGTH_LONG).show();
                        }else if (keeper.equals("next") || keeper.equals("next song") || keeper.equals("play the next song")){
                            next();
                            Toast.makeText(SmartPlayerActivity.this, "Command: "+ keeper, Toast.LENGTH_LONG).show();
                        }else if (keeper.equals("previous") || keeper.equals("previous song") || keeper.equals("play the previous song")){
                            previous();
                            Toast.makeText(SmartPlayerActivity.this, "Command: "+ keeper, Toast.LENGTH_LONG).show();
                        }else if (keeper.equals("voice off") || keeper.equals("no voice") || keeper.equals("turn off voice") || keeper.equals("turn voice off")){
                            voiceToggle();
                            Toast.makeText(SmartPlayerActivity.this, "Command: "+ keeper, Toast.LENGTH_LONG).show();
                        }

                        Toast.makeText(SmartPlayerActivity.this, "Result = " + keeper, Toast.LENGTH_LONG).show();

                    }
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

                if(voiceMode) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            speechRecognizer.startListening(speechRecognizerIntent);
                            keeper = "";
                            break;

                        case MotionEvent.ACTION_UP:
                            speechRecognizer.stopListening();
                            break;

                    }
                }
                return false;
            }
        });

        voiceEnableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             voiceToggle();
            }
        });

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.getCurrentPosition() > 0) {
                    previous();
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.getCurrentPosition() > 0) {
                    next();
                }
            }
        });






    }

    private void validateReceiveStart(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.reset();
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

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                next();
            }
        });

        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();

        playCycle();






        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                  if (progress==mediaPlayer.getDuration()){
                    Log.d(mediaPlayer.getDuration()+"", "onProgressChanged: ");       next();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void playCycle(){
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        startTime.setText(getTimeString(mediaPlayer.getCurrentPosition()));
        endTime.setText(getTimeString(mediaPlayer.getDuration()));

        if(mediaPlayer.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {

                    playCycle();
                }
            };
            handler.postDelayed(runnable, 1000 );
        }


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
            imageView.setBackgroundResource(R.drawable.five);
            pausePlayBtn.setImageResource(R.drawable.play);
            mediaPlayer.pause();

        }else{
            pausePlayBtn.setImageResource(R.drawable.pause);
            mediaPlayer.start();
            imageView.setBackgroundResource(R.drawable.four);

        }
    }

    private void play(){
        if (!mediaPlayer.isPlaying()){
            pausePlayBtn.setImageResource(R.drawable.pause);
            mediaPlayer.start();
            imageView.setBackgroundResource(R.drawable.four);

        }
    }

    private void pause(){
        if(mediaPlayer.isPlaying()){
            imageView.setBackgroundResource(R.drawable.five);
            pausePlayBtn.setImageResource(R.drawable.play);
            mediaPlayer.pause();
        }
    }

    private void next(){



        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        position = (position+1)%songs.size();

        File file = new File(songs.get(position).toString());
        songName= file.getName();
        songNameText.setText(songName);

        songNameText.setSelected(true);


        Uri uri = Uri.parse(songs.get(position).toString());

        mediaPlayer = MediaPlayer.create(SmartPlayerActivity.this, uri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                next();
            }
        });
        seekBar.setMax(mediaPlayer.getDuration());
        playPause();
        mediaPlayer.start();
        playCycle();
        imageView.setBackgroundResource(R.drawable.four);



    }

    private void previous() {
        mediaPlayer.pause();
        mediaPlayer.stop();
        mediaPlayer.release();

        position = (position - 1) < 0 ? (songs.size() - 1) : (position - 1);
        Uri uri = Uri.parse(songs.get(position).toString());

        mediaPlayer = mediaPlayer.create(SmartPlayerActivity.this, uri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                next();
            }
        });
        File file = new File(songs.get(position).toString());
        songName = file.getName();
        songNameText.setText(songName);

        playPause();
        mediaPlayer.start();

        playCycle();

        imageView.setBackgroundResource(R.drawable.four);

    }

    private void voiceToggle(){
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

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        long hours = millis / (1000*60*60);
        long minutes = ( millis % (1000*60*60) ) / (1000*60);
        long seconds = ( ( millis % (1000*60*60) ) % (1000*60) ) / 1000;

        buf.append(String.format("%02d", hours));
                buf.append(":");
                buf.append(String.format("%02d", minutes));
                buf.append(":");
                buf.append(String.format("%02d", seconds));

        return buf.toString();
    }


    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            mediaPlayer.stop();
            finish();
            Toast.makeText(this, "Player Stopped", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Press Back Again To Stop The Player And Select A New Song...", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();

    }




}
