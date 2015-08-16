package com.dnbitstudio.spotifystreamer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.dnbitstudio.spotifystreamer.ArtistSearchActivity;
import com.dnbitstudio.spotifystreamer.PlayTrackActivity;
import com.dnbitstudio.spotifystreamer.PlayTrackActivityFragment;
import com.dnbitstudio.spotifystreamer.R;
import com.dnbitstudio.spotifystreamer.models.CustomTrack;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindBool;

public class PlayTrackService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
    public static final String ARGS_BUNDLE = "args_bundle_key";
    public static final String RECEIVER_TAG = "receiverTag_key";
    public static final int NOTIFY_MP_PREPARED = 0;
    public static final int NOTIFY_TRACK_COMPLETED = 1;
    public static final int NOTIFICATION_ID = 0;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private final IBinder playTrackBinder = new PlayTrackBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<CustomTrack> tracks;
    private int trackNumber = -1;
    private int trackPosition;
    private CustomTrack track;
    private ResultReceiver playTrackResultReceiver;
    private boolean trackCompleted = false;
    private boolean sameTrack = false;
    private boolean fromNotification = false;
    @BindBool(R.bool.sw600)
    boolean mTwoPane;
    private boolean visibleControls;
    public static final String ACTION_CALL_PREVIOUS = "call_previous";
    public static final String ACTION_PAUSE = "call_pause";
    public static final String ACTION_CALL_PLAY = "call_play";
    public static final String ACTION_CALL_NEXT = "call_next";
    public static final String ACTION_TOGGLE_CONTROLS_VISIBILITY = "toggle_controls_visibility";

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        visibleControls = sharedPreferences.getBoolean(getString(R.string.pref_controls_key), true);

        if (intent != null)
        {
            String action = intent.getAction();
            if (action != null)
            {
                fromNotification = true;
                switch (action)
                {
                    case ACTION_CALL_PREVIOUS:
                        playPrevious();
                        break;
                    case ACTION_PAUSE:
                        pause();
                        createNotification(true);
                        break;
                    case ACTION_CALL_PLAY:
                        restartTrack();
                        createNotification(false);
                        break;
                    case ACTION_CALL_NEXT:
                        playNext();
                        break;
                    case ACTION_TOGGLE_CONTROLS_VISIBILITY:
                        createNotification(false);
                        break;
                    default:
                        break;
                }
            } else
            {
                fromNotification = false;
                // set the receiver every time onStartCommand is called
                // to ensure we use the fragment which is actually
                // associated with our activity
                playTrackResultReceiver = intent.getParcelableExtra(RECEIVER_TAG);

                Bundle args = intent.getBundleExtra(ARGS_BUNDLE);
                if (args != null && args.size() > 0)
                {
                    boolean same = checkIfSameTrack(args);
                    setSameTrack(same);

                    tracks = args.getParcelableArrayList(PlayTrackActivityFragment.TRACKS);
                    trackNumber = args.getInt(PlayTrackActivityFragment.TRACK_NUMBER);

                    if (tracks != null && tracks.get(trackNumber) != null)
                    {
                        track = tracks.get(trackNumber);
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        if (!fromNotification)
        {
            Bundle returnBundle = new Bundle();
            playTrackResultReceiver.send(NOTIFY_MP_PREPARED, returnBundle);
        }

        // This is a new track so we want the play button
        // not to be visible on the notification
        createNotification(false);

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
        if (!fromNotification)
        {
            Bundle returnBundle = new Bundle();
            playTrackResultReceiver.send(NOTIFY_TRACK_COMPLETED, returnBundle);
        }
    }

    public void initMediaPlayer()
    {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public void createNotification(boolean forcePlayVisible)
    {
        // Create explicit intent for our Activity
        Intent resultIntent;
        if (mTwoPane)
        {
            resultIntent = new Intent(getApplicationContext(), ArtistSearchActivity.class);
        } else
        {
            resultIntent = new Intent(getApplicationContext(), PlayTrackActivity.class);
        }

        // Fill intent with necessary data
        resultIntent.putParcelableArrayListExtra(PlayTrackActivityFragment.TRACKS, tracks);
        resultIntent.putExtra(PlayTrackActivityFragment.TRACK_NUMBER, trackNumber);


//resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        if(mTwoPane){
//            stackBuilder.addParentStack(ArtistSearchActivity.class);
//        } else {
//        stackBuilder.addParentStack(PlayTrackActivity.class);
//        }
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//
//
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );


//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // mId allows you to update the notification later on.
//        mNotificationManager.notify(0, builder.build());

        // Create pending intent and include the intent
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Use a remote view to have our own custom notification layout
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notifications);
        remoteView.setTextViewText(R.id.notification_track_name, track.getName());

        // Set controls if they are visible
        if (visibleControls)
        {
            setNotificationControls(remoteView, forcePlayVisible);
        } else
        {
            hideNotificationButtons(remoteView);
        }

        // Create a notification with the app icon,
        // the remote view and the pending intent
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContent(remoteView)
                        .setContentIntent(resultPendingIntent);

        // build the notification
        Notification notification = builder.build();

        // Include image in the remote view with the right id
        Picasso.with(getApplicationContext())
                .load(track.getUrl_large())
                .into(remoteView, R.id.notification_thumbnail, NOTIFICATION_ID, notification);

        // start foreground service with the right id and the notification
        startForeground(NOTIFICATION_ID, notification);
    }

    public void setNotificationControls(RemoteViews remoteView, boolean forcePlayVisible)
    {
        Intent intentPrevious = new Intent(getApplicationContext(), PlayTrackService.class);
        intentPrevious.setAction(ACTION_CALL_PREVIOUS);
        PendingIntent playPreviousPendingIntent =
                PendingIntent.getService(getApplicationContext(), 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPause = new Intent(getApplicationContext(), PlayTrackService.class);
        intentPause.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent =
                PendingIntent.getService(getApplicationContext(), 0, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlay = new Intent(getApplicationContext(), PlayTrackService.class);
        intentPlay.setAction(ACTION_CALL_PLAY);
        PendingIntent playPendingIntent =
                PendingIntent.getService(getApplicationContext(), 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(getApplicationContext(), PlayTrackService.class);
        intentNext.setAction(ACTION_CALL_NEXT);
        PendingIntent playNextPendingIntent =
                PendingIntent.getService(getApplicationContext(), 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteView.setOnClickPendingIntent(R.id.btn_notification_previous, playPreviousPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.btn_notification_pause, pausePendingIntent);
        remoteView.setOnClickPendingIntent(R.id.btn_notification_play, playPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.btn_notification_next, playNextPendingIntent);

        if (forcePlayVisible && !isPlaying())
        {
            remoteView.setViewVisibility(R.id.btn_notification_pause, View.GONE);
            remoteView.setViewVisibility(R.id.btn_notification_play, View.VISIBLE);
        } else
        {
            remoteView.setViewVisibility(R.id.btn_notification_play, View.GONE);
            remoteView.setViewVisibility(R.id.btn_notification_pause, View.VISIBLE);
        }

        remoteView.setViewVisibility(R.id.btn_notification_previous, View.VISIBLE);
        remoteView.setViewVisibility(R.id.btn_notification_next, View.VISIBLE);
    }

    public void hideNotificationButtons(RemoteViews remoteView)
    {
        remoteView.setViewVisibility(R.id.btn_notification_previous, View.GONE);
        remoteView.setViewVisibility(R.id.btn_notification_play, View.GONE);
        remoteView.setViewVisibility(R.id.btn_notification_pause, View.GONE);
        remoteView.setViewVisibility(R.id.btn_notification_next, View.GONE);
    }

    public boolean checkIfSameTrack(Bundle args)
    {
        boolean same = false;
        int newTrackNumber = args.getInt(PlayTrackActivityFragment.TRACK_NUMBER);
        // if track number is the same it may be the same track
        if (trackNumber == newTrackNumber)
        {
            ArrayList<CustomTrack> newTracks = args.getParcelableArrayList(PlayTrackActivityFragment.TRACKS);
            if (newTracks != null && newTracks.size() >= trackNumber)
            {
                CustomTrack newTrack = newTracks.get(trackNumber);

                // if the preview url is the same then it is the same track
                if (newTrack.getPreview_url().equals(track.getPreview_url()))
                {
                    same = true;
                }
            }
        }
        return same;
    }

    public void playNewTrack()
    {
        try
        {
            mediaPlayer.reset();
            track = tracks.get(trackNumber);
            mediaPlayer.setDataSource(track.getPreview_url());

            // if we are playing a new track then it is not the same track
            setSameTrack(false);

            // and it is starting so it is not completed
            setTrackCompleted(false);

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
//        setTrackCompleted(false);
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

    public boolean isSameTrack()
    {
        return sameTrack;
    }

    public void setSameTrack(boolean sameTrack)
    {
        this.sameTrack = sameTrack;
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