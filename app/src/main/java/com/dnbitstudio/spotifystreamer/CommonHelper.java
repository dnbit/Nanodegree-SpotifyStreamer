package com.dnbitstudio.spotifystreamer;

import android.app.Activity;
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


    public static String getImageURL(Activity activity, List<Image> images, int minWidth)
    {
        int size = images.size();
        if (size > 0)
        {
            ListIterator iterator = images.listIterator(size);
            // We want the smallest with width >= MIN_IMAGE_SIZE_SMALL
            while (iterator.hasPrevious())
            {
                Image image = (Image) iterator.previous();
                if (iterator.hasPrevious())
                {
                    if (image.width >= minWidth)
                    {
                        return image.url;
                    }
                }
            }
        }
        return "";
    }
}