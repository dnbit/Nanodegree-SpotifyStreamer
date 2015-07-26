package com.dnbitstudio.spotifystreamer.services;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class PlayTrackResultReceiver extends ResultReceiver
{
    private Receiver mReceiver;

    public PlayTrackResultReceiver(Handler handler)
    {
        super(handler);
    }

    public interface Receiver
    {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver)
    {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData)
    {

        if (mReceiver != null)
        {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}