package com.dnbitstudio.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A lightweight and parcelable custom Track class
 */
public class CustomTrack implements Parcelable
{
    String name;
    String album;
    String url;
    String id;

    public CustomTrack(String name, String album, String url, String id)
    {
        this.name = name;
        this.album = album;
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

    public String getAlbum()
    {
        return album;
    }

    public void setAlbum(String album)
    {
        this.album = album;
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

    protected CustomTrack(Parcel in) {
        name = in.readString();
        album = in.readString();
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
        dest.writeString(album);
        dest.writeString(url);
        dest.writeString(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CustomTrack> CREATOR = new Parcelable.Creator<CustomTrack>() {
        @Override
        public CustomTrack createFromParcel(Parcel in) {
            return new CustomTrack(in);
        }

        @Override
        public CustomTrack[] newArray(int size) {
            return new CustomTrack[size];
        }
    };
}