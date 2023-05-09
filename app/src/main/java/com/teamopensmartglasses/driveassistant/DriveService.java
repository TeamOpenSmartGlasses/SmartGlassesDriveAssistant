package com.teamopensmartglasses.driveassistant;

import android.os.Handler;
import android.util.Log;

import com.teamopensmartglasses.driveassistant.events.ObdConnectedEvent;
import com.teamopensmartglasses.driveassistant.events.ObdDisconnectedEvent;
import com.teamopensmartglasses.driveassistant.events.ObdFoundPairedDeviceEvent;
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
    final int delay = 2000; // 1000 milliseconds == 1 second
    public String tachString = "X";
    public String speedString = "X";
    public String fuelString = "X";
    public String mpgString = "X";
    boolean displayRefreshStarted = false;
    public boolean displayBottom = true;
    public boolean displayRight = true;
    public String padding = "";
    FocusHandler focusHandler;
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

        /* Handle SGMLib stuff */

        //Create SGMLib instance with context: this
        sgmLib = new SGMLib(this);

        //Define command with a UUID
        UUID commandUUID = UUID.fromString("5b824bb6-d3b3-417d-8c74-3b103efb403d");
        SGMCommand command = new SGMCommand(appName, commandUUID, new String[]{"Drive assistant"}, "Speed/tach on smartglasses!");

        //Register the command
        sgmLib.registerCommand(command, this::driveCommandCallback);

        focusHandler = new FocusHandler();

        /* Handle DriveAssistant stuff */

        Log.d(TAG, "DRIVE ASSISTANT SERVICE STARTED");

        EventBus.getDefault().register(this);
        obdManager = new ObdManager();
        padding = maybeGeneratePadding();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        EventBus.getDefault().unregister(this);
        stopObdTasks();
        displayAppStopped("Service closed");
        sgmLib.deinit();
        super.onDestroy();
    }

    public void driveCommandCallback(String args, long commandTriggeredTime){
        Log.d(TAG,"Drive callback called");
        sgmLib.requestFocus(focusHandler); //Request SGM's focus

        // Make sure we aren't already connected...
        stopObdTasks();
        obdManager = new ObdManager();

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
                        //Log.d(TAG,"NEW TACH: " + manager.getTach());
                        tachString = String.valueOf(manager.getTach());

                        if(!displayRefreshStarted)
                            startDriveDisplayRefresh();
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
        displayRefreshStarted = true;
        handler.postDelayed(new Runnable() {
            public void run() {
                String toSend = padding;
                toSend += tachString + "rpm | " + speedString + "mph";

                sgmLib.sendReferenceCard("", toSend);

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    /* Subscriptions */
    @Subscribe
    public void onObdFoundEvent(ObdFoundPairedDeviceEvent receivedEvent){ sgmLib.sendReferenceCard(appName, "Found paired OBDII device...\nConnecting..."); }
    @Subscribe
    public void onObdConnectedEvent(ObdConnectedEvent receivedEvent){
        startDriveDisplayRefresh();
    }

    @Subscribe
    public void onObdDisonnectedEvent(ObdDisconnectedEvent receivedEvent){
        Log.d(TAG, "DISCONNECTED OR FAILED TO CONNECT... STOPPING DRIVE ASSISTANT");
        stopObdTasks();
        displayAppStopped(receivedEvent.reason);
        stopForeground(true);
        stopSelf();
    }

    /* Helpers */
    public String maybeGeneratePadding(){
        String pad = "";
        if(displayBottom) {
            for (int i = 0; i < 7; i++) {
                //25 spaces = 1 line on ActiveLook Engo 2
                pad += "                         ";
            }
        }
        if(displayRight){
            pad += ""; //TODO: investigate weirdness here
        }
        return pad;
    }

    public void displayAppStopped(String reason){
        sgmLib.sendReferenceCard(appName, appName + " stopped:    " + reason);
    }

    public void stopObdTasks(){
        obdManager.Disconnect();

        //Cancel screen refresh
        handler.removeCallbacksAndMessages(null);
        displayRefreshStarted = false;
    }
}
