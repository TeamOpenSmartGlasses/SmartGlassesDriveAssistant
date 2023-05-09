package com.teamopensmartglasses.driveassistant;

import android.util.Log;

import com.teamopensmartglasses.driveassistant.events.FocusChangedEvent;
import com.teamopensmartglasses.driveassistant.events.ObdDisconnectedEvent;
import com.teamopensmartglasses.sgmlib.FocusCallback;
import com.teamopensmartglasses.sgmlib.FocusStates;

import org.greenrobot.eventbus.EventBus;

public class FocusHandler implements FocusCallback {
    final String TAG = "SmartGlassesDriveAssistant_FocusHandler";
    @Override
    public void runFocusChange(FocusStates focusState) {
        if(focusState == FocusStates.OUT_FOCUS){}
    }
}
