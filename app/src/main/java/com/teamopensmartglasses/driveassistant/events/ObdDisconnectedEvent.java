package com.teamopensmartglasses.driveassistant.events;

public class ObdDisconnectedEvent {
    public String reason;
    public ObdDisconnectedEvent(String myReason){reason = myReason;}
}
