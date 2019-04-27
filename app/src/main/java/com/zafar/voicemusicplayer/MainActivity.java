package com.zafar.voicemusicplayer;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songsList = findViewById(R.id.songNames);
        appExternalStoragePermission();

    }

    public void appExternalStoragePermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        displaySongNames();
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


    }

    @Override
    public void onBackPressed() {

        // Gives time between back press and closing activity, if pressed accidentally
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finish();
        } else {
            Toast.makeText(this, "Press Back Again To Exit", Toast.LENGTH_SHORT).show();
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