package com.dnbitstudio.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A lightweight and parcelable custom Artist class
 */
public class CustomArtist implements Parcelable
{
    String name;
    String url;
    String id;

    public CustomArtist(String name, String url, String id)
    {
        this.name = name;
        this.url = url;
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    protected CustomArtist(Parcel in) {
        name = in.readString();
        url = in.readString();
        id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CustomArtist> CREATOR = new Parcelable.Creator<CustomArtist>() {
        @Override
        public CustomArtist createFromParcel(Parcel in) {
            return new CustomArtist(in);
        }

        @Override
        public CustomArtist[] newArray(int size) {
            return new CustomArtist[size];
        }
    };
}