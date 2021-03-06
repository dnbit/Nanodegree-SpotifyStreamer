package com.dnbitstudio.spotifystreamer;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;
import java.util.ListIterator;

import kaaes.spotify.webapi.android.models.Image;

/**
 * A helper for common purposes
 */
public class CommonHelper
{
    public static boolean isNetworkConnected(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static String getImageURL(List<Image> images, int minWidth)
    {
        int size = images.size();
        if (size > 0)
        {
            ListIterator iterator = images.listIterator(size);
            // We want the smallest with width >= MIN_IMAGE_SIZE_SMALL
            while (iterator.hasPrevious())
            {
                Image image = (Image) iterator.previous();
                    if (image.width >= minWidth)
                    {
                        return image.url;
                    }
            }
        }
        return null;
    }

    // Method to know if a given service is running
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}