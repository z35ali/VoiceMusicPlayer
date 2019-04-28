package com.zafar.voicemusicplayer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class SmartPlayerActivity extends AppCompatActivity {

    private RelativeLayout parentRelativeLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private ImageView pausePlayBtn, nextBtn, previousBtn, loopBtn;
    private TextView songNameText, startTime, endTime;
    private ImageView imageView;
    private RelativeLayout lowerLayout;
    private Button voiceEnableBtn;
    SeekBar seekBar;
    private boolean voiceMode;

    private static MediaPlayer mediaPlayer;
    private int position;
    private ArrayList < File > songs;
    private static String songName;
    private  static String title;
    private boolean playing = false;
    private boolean loop;



    Handler handler;
    Runnable runnable;

    private boolean noRecordPermission = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_player);


        // Declare layouts
        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);
        pausePlayBtn = findViewById(R.id.pause_play_btn);
        nextBtn = findViewById(R.id.next_btn);
        previousBtn = findViewById(R.id.previous_btn);
        loopBtn = findViewById(R.id.loop_btn);
        imageView = findViewById(R.id.logo);
        lowerLayout = findViewById(R.id.lower);
        voiceEnableBtn = findViewById(R.id.voice_enable_btn);
        songNameText = findViewById(R.id.songInfo);
        seekBar = findViewById(R.id.seekBar);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);

        // Disable voice commands by default
        lowerLayout.setVisibility(View.VISIBLE);
        voiceMode = false;
        loop = false;



                handler = new Handler();
                checkRecordPermission();

        showNotification();

        // Speech Recognizer initialization
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(SmartPlayerActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());




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

                ArrayList < String > matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matchesFound != null) {

                    // If voiceMode is true check if spoken command equals required commands
                    if (voiceMode) {
                        keeper = matchesFound.get(0);

                        if (keeper.equals("pause") || keeper.equals("pause the song") || keeper.equals("pause song")) {
                            pause();
                            Toast.makeText(SmartPlayerActivity.this, "Command: " + keeper, Toast.LENGTH_LONG).show();
                        } else if (keeper.equals("play") || keeper.equals("play the song") || keeper.equals("play song")) {
                            play();
                            Toast.makeText(SmartPlayerActivity.this, "Command: " + keeper, Toast.LENGTH_LONG).show();
                        } else if (keeper.equals("next") || keeper.equals("next song") || keeper.equals("play the next song")) {
                            next();
                            Toast.makeText(SmartPlayerActivity.this, "Command: " + keeper, Toast.LENGTH_LONG).show();
                        } else if (keeper.equals("previous") || keeper.equals("previous song") || keeper.equals("play the previous song")) {
                            previous();
                            Toast.makeText(SmartPlayerActivity.this, "Command: " + keeper, Toast.LENGTH_LONG).show();
                        } else if (keeper.equals("voice off") || keeper.equals("no voice") || keeper.equals("turn off voice") || keeper.equals("turn voice off")) {
                            voiceToggle();
                            Toast.makeText(SmartPlayerActivity.this, "Command: " + keeper, Toast.LENGTH_LONG).show();
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

        // Allows long press for voice mode
        parentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (voiceMode) {
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

        loopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loop) {
                    loopBtn.setColorFilter(Color.BLACK);
                    loop = false;
                } else{
                    loopBtn.setColorFilter(Color.RED);
                    loop = true;
                }
            }
        });

    }







   private void showNotification() {
      NotificationManager mNotificationManager;

       NotificationCompat.Builder mBuilder =
               new NotificationCompat.Builder(getApplicationContext(), "notify_001");
       Intent intent = new Intent(this, SmartPlayerActivity.class);
       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

       PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

       NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();

       if (playing) {
           bigText.setBigContentTitle(title + " is playing!");
       }else{
           bigText.setBigContentTitle(title + " is paused!");
       }

       mBuilder.setContentIntent(pendingIntent);
       mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
       mBuilder.setContentText("Tap to go back to player");
       mBuilder.setPriority(Notification.PRIORITY_MIN);
       mBuilder.setStyle(bigText);

       mNotificationManager =
               (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


       if (Build.VERSION.SDK_INT >= 26)
       {
           String channelId = "Your_channel_id";
           NotificationChannel channel = new NotificationChannel(channelId,
                   "Channel readable title", NotificationManager.IMPORTANCE_DEFAULT);
           channel.enableVibration(false);
           channel.setSound(null, null);
           mBuilder.setOngoing(true);
           mNotificationManager.createNotificationChannel(channel);
           mBuilder.setChannelId(channelId);

       }

       mNotificationManager.notify(0, mBuilder.build());
   }

    public void checkRecordPermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                       validateReceiveStart();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                    noRecordPermission = true;
                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    private void validateReceiveStart() {

        // Stops the current media player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }


        // Gets file list from Main Activity and sets appropriate variables
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        position = bundle.getInt("position", 0);
        songs = (ArrayList) bundle.getParcelableArrayList("song");
        songName = songs.get(position).getName();
        String mSongName = intent.getStringExtra("name");

        // Gets current song and creates a media player for it
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(SmartPlayerActivity.this, uri);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                if (loop){
                    loopSong();
                }else{
                    next();
                }

            }
        });

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songs.get(position).toString());
        byte [] data = mmr.getEmbeddedPicture();
        getTrackInfo(songs.get(position).toString(), songName);

        if (!(data == null)) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            imageView.setImageBitmap(bitmap);
        }else{
            imageView.setImageResource(R.drawable.music);
        }

        // Set seek bar to end at song ending
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        playing = true;
        showNotification();

        playCycle();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                    startTime.setText(getTimeString(mediaPlayer.getCurrentPosition()));
                    endTime.setText(getTimeString(mediaPlayer.getDuration()));

                    // When the seek bar is pushed to the end play the next song
                    if (progress == mediaPlayer.getDuration()) {
                        next();
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

    private void playCycle() {

        // Move Seek Bar progress as well as start and end times on each playCycle() call
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        startTime.setText(getTimeString(mediaPlayer.getCurrentPosition()));
        endTime.setText(getTimeString(mediaPlayer.getDuration()));

        // Handles accidental notification close
        showNotification();


        if (mediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {

                    // Recursively runs function while media is playing
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 1000);
        }


    }




    private void playPause() {

        if (mediaPlayer.isPlaying()) {
            pausePlayBtn.setImageResource(R.drawable.play);
            mediaPlayer.pause();
            playing = false;


        } else {
            pausePlayBtn.setImageResource(R.drawable.pause);
            mediaPlayer.start();
            playing = true;
        }
        showNotification();

    }

    private void play() {
        if (!mediaPlayer.isPlaying()) {
            pausePlayBtn.setImageResource(R.drawable.pause);
            mediaPlayer.start();
            playing = true;
            showNotification();
        }
    }

    private void pause() {
        if (mediaPlayer.isPlaying()) {
            pausePlayBtn.setImageResource(R.drawable.play);
            mediaPlayer.pause();
            playing = false;
            showNotification();
        }
    }

    private void next() {
        changeSong((position + 1) % songs.size());
    }

    private void previous() {
        changeSong((position - 1) < 0 ? (songs.size() - 1) : (position - 1));
    }

    private void loopSong() {
        changeSong((position) % songs.size());
        mediaPlayer.start();
    }

    private void changeSong(int pos) {

        // Stops the current media player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        position = pos;

        // Parses new song
        Uri uri = Uri.parse(songs.get(position).toString());

        // Creates a new media player
        mediaPlayer = mediaPlayer.create(SmartPlayerActivity.this, uri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                if (loop){
                    loopSong();
                }else{
                    next();
                }
            }
        });

        // Sets the songName Textview to the basename of the file path
        File file = new File(songs.get(position).toString());
        songName = file.getName();


        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(songs.get(position).toString());
        byte [] data = mmr.getEmbeddedPicture();
        getTrackInfo(songs.get(position).toString(), songName);

        if (!(data == null)) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            imageView.setImageBitmap(bitmap);
        }else{
            imageView.setImageResource(R.drawable.music);
        }

        playPause();

        // Set seek bar to end at song ending
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();


        playCycle();


    }

    private void voiceToggle() {

        if (!noRecordPermission) {
            // Toggles voice mode on and off as well as changing button text
            if (voiceMode) {
                voiceEnableBtn.setText("Voice Enabled - OFF");
                voiceMode = false;
                lowerLayout.setVisibility(View.VISIBLE);
            } else {
                voiceEnableBtn.setText("Voice Enabled - ON \nHold Anywhere to Trigger");
                lowerLayout.setVisibility(View.GONE);
                voiceMode = true;

            }
        }else{
            Toast.makeText(this, "No Record Permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = ((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        // Creates a buffered String that is the converted song length into hours, minutes, and seconds
        buf.append(String.format("%02d", hours));
        buf.append(":");
        buf.append(String.format("%02d", minutes));
        buf.append(":");
        buf.append(String.format("%02d", seconds));

        return buf.toString();
    }

    private void getTrackInfo(String path, String fileName) {
        MediaMetadataRetriever metaRetriever= new MediaMetadataRetriever();
        metaRetriever.setDataSource(path);
        String artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
         title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String album = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        if (artist == null || title == null || album == null) {
            songNameText.setText("\nFile: "+ fileName+"\nNo Song Information Found");
            title = "Unknown Song";

        }else{
            songNameText.setText("Song: "+ title + "\nArtist: "+artist+"\nAlbum: "+ album);
        }
        songNameText.setSelected(true);


    }

    public static String getCurrentSong(){
        return songName;
    }

    public static MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Intent intent = new Intent(this, MainActivity.class);
        startActivityIfNeeded(intent, 0);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getSystemService(ns);
        nMgr.cancel(0);
    }
}