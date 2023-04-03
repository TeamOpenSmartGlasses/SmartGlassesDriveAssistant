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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.teamopensmartglasses.sgmlib.SGMCommand;
import com.teamopensmartglasses.sgmlib.SGMLib;
import com.teamopensmartglasses.sgmlib.SmartGlassesAndroidService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.UUID;

public class DriveService extends SmartGlassesAndroidService {
    public final String TAG = "DriveAssistantApp_DriveService";
    static final String appName = "Drive Assistant";
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
                appName,
                "Drive Assistant for smartglasses", com.google.android.material.R.drawable.notify_panel_notification_icon_bg);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Create SGMLib instance with context: this
        sgmLib = new SGMLib(this);

        //Define command with a UUID
        UUID commandUUID = UUID.fromString("5b824bb6-d3b3-417d-8c74-3b103efb403d");
        SGMCommand command = new SGMCommand(appName, commandUUID, new String[]{"Drive assistant"}, "Speed/tach on smartglasses!");

        //Register the command
        sgmLib.registerCommand(command, this::driveCommandCallback);

        Log.d(TAG, "DRIVE ASSISTANT SERVICE STARTED");

        EventBus.getDefault().register(this);
        obdManager = new ObdManager();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        EventBus.getDefault().unregister(this);
        stopObdTasks("");
        sgmLib.deinit();
        super.onDestroy();
    }

    public void driveCommandCallback(String args, long commandTriggeredTime){
        Log.d(TAG,"Drive callback called");
        sgmLib.sendReferenceCard(appName, "Searching for OBDII connection...");
        obdManager.Connect();
        listenToDriveStuff();
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
                //System.out.println("myHandler: here!"); // Do your work here

                String toSend = speedString + "mph | " + tachString + "rpm";
                sgmLib.sendReferenceCard(appName, toSend);

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void stopObdTasks(String reason){
        obdManager.Disconnect();
        handler.removeCallbacksAndMessages(null);
        sgmLib.sendReferenceCard(appName, appName + " stopped:\n" + reason);
    }

    @Subscribe
    public void onObdConnectedEvent(ObdConnectedEvent receivedEvent){
        startDriveDisplayRefresh();
    }

    @Subscribe
    public void onObdDisonnectedEvent(ObdDisconnectedEvent receivedEvent){
        Log.d(TAG, "DISCONNECTED OR FAILED TO CONNECT... STOPPING DRIVE ASSISTANT");
        stopObdTasks(receivedEvent.reason);

        stopForeground(true);
        stopSelf();
    }
}
