package com.zafar.voicemusicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();

        String action=intent.getStringExtra("action");
        if(action.equals("actionPrev")){
            performAction1();
            Toast.makeText(context, "Prev", Toast.LENGTH_SHORT).show();
        }
        else if(action.equals("actionPP")){
            performAction2();
            Toast.makeText(context, "Pause/Play", Toast.LENGTH_SHORT).show();


        } else if(action.equals("actionNext")){
            performAction3();
            Toast.makeText(context, "Next", Toast.LENGTH_SHORT).show();


        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);

    }

    public void performAction1(){

    }

    public void performAction2(){

    }
    
    public void performAction3(){

    }

}