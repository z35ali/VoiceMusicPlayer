package com.zafar.voicemusicplayer;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String[] itemsAll;
    private ListView songsList;
    private long backPressedTime;
    private ImageView refresh;
    private  Animation animation;
    Runnable runnable;

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songsList = findViewById(R.id.songNames);
        refresh = findViewById(R.id.refresh_btn);
        handler = new Handler();
        appExternalStoragePermission();
        displaySongNames();
       animation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(2000);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySongNames();
                ((ImageView)findViewById(R.id.refresh_btn)).setAnimation(animation);
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView)findViewById(R.id.refresh_btn)).clearAnimation();

                    }
                };
                handler.postDelayed(runnable, 2000);




            }
        });

    }

    public void appExternalStoragePermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {

                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    public ArrayList<File> readAudioSongs(File file){
        ArrayList<File> arrayList = new ArrayList<>();

        File[] allFiles = file.listFiles();

        for (File individualFile: allFiles){

            if (individualFile.isDirectory() && !individualFile.isHidden()){
                arrayList.addAll(readAudioSongs(individualFile));
            }else{
                if (individualFile.getName().endsWith(".mp3") || individualFile.getName().endsWith(".aac") || individualFile.getName().endsWith(".wav") || individualFile.getName().endsWith(".wma")){
                    arrayList.add(individualFile);
                }
            }
        }
        return arrayList;
    }

    private void displaySongNames(){
        final ArrayList<File> songs = readAudioSongs(Environment.getExternalStorageDirectory());
        itemsAll = new String[songs.size()];

        for(int songCounter = 0; songCounter<songs.size(); songCounter++){
            itemsAll[songCounter] = songs.get(songCounter).getName();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, itemsAll);
        songsList.setAdapter(arrayAdapter);

        songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = songsList.getItemAtPosition(position).toString();


                if (SmartPlayerActivity.getCurrentSong() != null && SmartPlayerActivity.getCurrentSong().equals(songName) ) {
                    Intent openMainActivity = new Intent(getApplicationContext(), SmartPlayerActivity.class);
                    openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityIfNeeded(openMainActivity, 0);
                } else {


                    Intent intent = new Intent(MainActivity.this, SmartPlayerActivity.class);
                    intent.putExtra("song", songs);
                    intent.putExtra("songName", songName);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
            }


        });

        songsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // If long click play another song restart current song
                String songName = songsList.getItemAtPosition(position).toString();
                Intent intent = new Intent(MainActivity.this, SmartPlayerActivity.class);
                intent.putExtra("song", songs);
                intent.putExtra("songName", songName);
                intent.putExtra("position", position);
                startActivity(intent);
                return false;
            }
        });


    }

    @Override
    public void onBackPressed() {

        // Gives time between back press and closing activity, if pressed accidentally
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (SmartPlayerActivity.getMediaPlayer() == null){
                finish();
            }else {
                Intent openMainActivity = new Intent(getApplicationContext(), SmartPlayerActivity.class);
                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityIfNeeded(openMainActivity, 0);
            }
        } else if (SmartPlayerActivity.getMediaPlayer() == null) {
            Toast.makeText(this, "Press Back Again To Exit", Toast.LENGTH_SHORT).show();

        }else{

            Toast.makeText(this, "Press Back Again To Player If Started", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getSystemService(ns);
        nMgr.cancel(0);
    }
}