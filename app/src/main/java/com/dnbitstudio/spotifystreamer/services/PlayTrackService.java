package com.dnbitstudio.spotifystreamer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;

import com.dnbitstudio.spotifystreamer.PlayTrackActivityFragment;
import com.dnbitstudio.spotifystreamer.models.CustomTrack;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class PlayTrackService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
    public static final String ARGS_BUNDLE = "args_bundle_key";
    public static final String RECEIVER_TAG = "receiverTag_key";
    public static final int NOTIFY_MP_PREPARED = 0;
    public static final int NOTIFY_TRACK_COMPLETED = 1;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final IBinder playTrackBinder = new PlayTrackBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<CustomTrack> tracks;
    private int trackNumber;
    private int trackPosition;
    private CustomTrack track;
    private ResultReceiver playTrackResultReceiver;
    private boolean trackCompleted = false;

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
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null)
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

            // set the receiver every time onStartCommand is called
            // to ensure we use the fragment which is actually
            // associated with our activity
            playTrackResultReceiver = intent.getParcelableExtra(RECEIVER_TAG);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        Bundle returnBundle = new Bundle();
        playTrackResultReceiver.send(NOTIFY_MP_PREPARED, returnBundle);

        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
    {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        setTrackCompleted(true);
        Bundle returnBundle = new Bundle();
        playTrackResultReceiver.send(NOTIFY_TRACK_COMPLETED, returnBundle);
    }

    public void initMediaPlayer()
    {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public void playNewTrack()
    {
        try
        {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(tracks.get(trackNumber).getPreview_url());

            // fetch image before starting to play the track
            String url = tracks.get(trackNumber).getUrl_large();
            if (url != null)
            {
                Picasso.with(getApplicationContext()).
                        load(url)
                        .fetch(new Callback()
                        {
                            @Override
                            public void onSuccess()
                            {
                                mediaPlayer.prepareAsync();
                            }

                            @Override
                            public void onError()
                            {
                                mediaPlayer.prepareAsync();
                            }
                        });
            } else
            {
                mediaPlayer.prepareAsync();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
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
        start();
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
        playNewTrack();
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
        playNewTrack();
    }

    // *********************************
    //    MEDIAPLAYER CONTROL METHODS
    // *********************************
    public void start()
    {
        setTrackCompleted(false);
        mediaPlayer.start();
    }

    public void pause()
    {
        mediaPlayer.pause();
    }

    public void seekTo(int millis)
    {
        mediaPlayer.seekTo(millis);
    }

    public int getDuration()
    {
        return mediaPlayer.getDuration();
    }

    public int getPosition()
    {
        return mediaPlayer.getCurrentPosition();
    }

    // *************************
    //    GETTERS AND SETTERS
    // *************************
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

    public boolean isTrackCompleted()
    {
        return trackCompleted;
    }

    public void setTrackCompleted(boolean trackCompleted)
    {
        this.trackCompleted = trackCompleted;
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