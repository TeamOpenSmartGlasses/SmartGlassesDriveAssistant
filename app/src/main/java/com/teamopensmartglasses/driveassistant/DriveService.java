package com.teamopensmartglasses.driveassistant;

import android.os.Handler;
import android.util.Log;

import com.google.android.material.R.drawable.*;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.teamopensmartglasses.sgmlib.SGMCommand;
import com.teamopensmartglasses.sgmlib.SGMLib;
import com.teamopensmartglasses.sgmlib.SmartGlassesAndroidService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.UUID;

public class DriveService extends SmartGlassesAndroidService {
    public final String TAG = "DriveAssistantApp_DriveService";
    public ObdManager obdManager;
    final Handler handler = new Handler();
    final int delay = 1000; // 1000 milliseconds == 1 second
    public String tachString = "X";
    public String speedString = "X";
    public String fuelString = "X";
    public String mpgString = "X";

    //our instance of the SGM library
    public SGMLib sgmLib;

    public DriveService(){
        super(MainActivity.class,
                "driveassistant_app",
                1002,
                "Drive Assistant",
                "Drive Assistant for smartglasses", com.google.android.material.R.drawable.notify_panel_notification_icon_bg);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Create SGMLib instance with context: this
        sgmLib = new SGMLib(this);

        //Define command with a UUID
        UUID commandUUID = UUID.fromString("5b824bb6-d3b3-417d-8c74-3b103efb403d");
        SGMCommand command = new SGMCommand("Drive Assistant", commandUUID, new String[]{"Drive assistant"}, "Speed/tach on smartglasses!");

        //Register the command
        sgmLib.registerCommand(command, this::driveCommandCallback);

        Log.d(TAG, "DRIVE ASSISTANT SERVICE STARTED");

        obdManager = new ObdManager();
        driveCommandCallback("",0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        handler.removeCallbacksAndMessages(null);
    }

    public void driveCommandCallback(String args, long commandTriggeredTime){
        Log.d("TAG","Drive callback called");
        sgmLib.sendReferenceCard("Drive Assistant", "Drive Assistant started");
        //Get ready
        obdManager.Connect();
        listenToDriveStuff();
        startDriveDisplayRefresh();
    }

    public void listenToDriveStuff(){
        final ObdManager.OnChangedListener obdListener =
                new ObdManager.OnChangedListener() {

                    @Override
                    public void onTachChanged(ObdManager manager) {
                        //tachString = String.format("%.1f", (manager.getTach() / 1000f));
                        //Log.d(TAG,"HOLYFUCK NEW TACH: " + manager.getTach());
                        tachString = String.valueOf(manager.getTach());
                    }

                    @Override
                    public void onSpeedChanged(ObdManager manager) {
                        speedString = Integer.toString(manager.getSpeed());
                    }

                    @Override
                    public void onMpgChanged(ObdManager manager) {
                        //mpgString = String.format("%.1d", manager.getSpeed());
                        //mpgString = "";
                    }

                    @Override
                    public void onFuelChanged(ObdManager manager) {
                        //fuelString = Integer.toString(manager.getFuel()) + "%";
                    }
                };
        obdManager.addOnChangedListener(obdListener);
    }

    public void startDriveDisplayRefresh(){
        handler.postDelayed(new Runnable() {
            public void run() {
                System.out.println("myHandler: here!"); // Do your work here

                String toSend = speedString + "mph | " + tachString + "rpm";
                sgmLib.sendReferenceCard("Drive Assistant", toSend);

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

}
