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
    String url_small;
    String url_large;
    String preview_url;
    String artist;

    public CustomTrack(String name, String album, String url_small,
                       String url_large, String preview_url, String artist)
    {
        this.name = name;
        this.album = album;
        this.url_small = url_small;
        this.url_large = url_large;
        this.preview_url = preview_url;
        this.artist = artist;
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

    public String getUrl_small()
    {
        return url_small;
    }

    public void setUrl_small(String url_small)
    {
        this.url_small = url_small;
    }

    public String getUrl_large()
    {
        return url_large;
    }

    public void setUrl_large(String url_large)
    {
        this.url_large = url_large;
    }

    public String getPreview_url()
    {
        return preview_url;
    }

    public void setPreview_url(String preview_url)
    {
        this.preview_url = preview_url;
    }

    public String getArtist()
    {
        return artist;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    protected CustomTrack(Parcel in) {
        name = in.readString();
        album = in.readString();
        url_small = in.readString();
        url_large = in.readString();
        preview_url = in.readString();
        artist = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(album);
        dest.writeString(url_small);
        dest.writeString(url_large);
        dest.writeString(preview_url);
        dest.writeString(artist);
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