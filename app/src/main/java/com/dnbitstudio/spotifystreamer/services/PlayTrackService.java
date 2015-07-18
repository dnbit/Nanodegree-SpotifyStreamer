package com.dnbitstudio.spotifystreamer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

import com.dnbitstudio.spotifystreamer.PlayTrackActivityFragment;
import com.dnbitstudio.spotifystreamer.models.CustomTrack;

import java.io.IOException;
import java.util.ArrayList;

public class PlayTrackService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener
{
    public static final String ARGS_BUNDLE = "args_bundle_key";
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final IBinder playTrackBinder = new PlayTrackBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<CustomTrack> tracks;
    private int trackNumber;
    private int trackPosition;
    private CustomTrack track;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        trackPosition = 0;
        initMediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return playTrackBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Bundle args = intent.getBundleExtra(ARGS_BUNDLE);
        if (args != null && args.size() > 0)
        {
            tracks = args.getParcelableArrayList(PlayTrackActivityFragment.TRACKS);
            trackNumber = args.getInt(PlayTrackActivityFragment.TRACK_NUMBER);

            if (tracks.get(trackNumber) != null)
            {
                track = tracks.get(trackNumber);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
    {
        mediaPlayer.reset();
        return false;
    }

    public void initMediaPlayer()
    {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void playTrack()
    {
        try
        {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(tracks.get(trackNumber).getPreview_url());
            mediaPlayer.prepareAsync();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // **************************
    //  PLAYBACK CONTROL METHODS
    // **************************
    public void pauseTrack()
    {
        mediaPlayer.pause();
    }

    public void restartTrack()
    {
        mediaPlayer.start();
    }

    public void playPrevious()
    {
        mediaPlayer.stop();
        if (trackNumber == 0)
        {
            trackNumber = tracks.size() - 1;
        } else
        {
            trackNumber = --trackNumber;
        }
        playTrack();
    }

    public void playNext()
    {
        mediaPlayer.stop();
        if (trackNumber == tracks.size() - 1)
        {
            trackNumber = 0;
        } else
        {
            trackNumber = ++trackNumber;
        }
        playTrack();
    }

    // *************************
    //    GETTERS AND SETTERS
    // *************************
    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer = mediaPlayer;
    }

    public ArrayList<CustomTrack> getTracks()
    {
        return tracks;
    }

    public void setTracks(ArrayList<CustomTrack> tracks)
    {
        this.tracks = tracks;
    }

    public int getTrackNumber()
    {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber)
    {
        this.trackNumber = trackNumber;
    }

    public int getTrackPosition()
    {
        return trackPosition;
    }

    public void setTrackPosition(int trackPosition)
    {
        this.trackPosition = trackPosition;
    }

    public CustomTrack getTrack()
    {
        return track;
    }

    public void setTrack(CustomTrack track)
    {
        this.track = track;
    }

    // *************************
    //        INNER CLASS
    // *************************
    public class PlayTrackBinder extends Binder
    {
        public PlayTrackService getService()
        {
            return PlayTrackService.this;
        }
    }
}