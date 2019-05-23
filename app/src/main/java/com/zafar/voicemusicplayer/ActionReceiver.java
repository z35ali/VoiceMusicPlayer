package com.zafar.voicemusicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        String action=intent.getStringExtra("action");
        if(action.equals("actionPrev")){
            Intent previousIntent = new Intent(context, SmartPlayerActivity.class);
            previousIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            SmartPlayerActivity.previous = true;
                context.startActivity(previousIntent);
            Toast.makeText(context, "Prev", Toast.LENGTH_SHORT).show();

        }
        else if(action.equals("actionPP")){
            Toast.makeText(context, "Pause/Play", Toast.LENGTH_SHORT).show();
            Intent openPlayerActivity= new Intent(context, SmartPlayerActivity.class);
            openPlayerActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK );


            SmartPlayerActivity.intentSent = true;
            context.startActivity(openPlayerActivity);




        } else if(action.equals("actionNext")){

            Intent nextIntent = new Intent(context, SmartPlayerActivity.class);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            SmartPlayerActivity.next = true;
            Toast.makeText(context, "Next", Toast.LENGTH_SHORT).show();
            context.startActivity(nextIntent);
        }
        /*This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it); */


    }



    }




