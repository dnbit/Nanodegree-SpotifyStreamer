package com.dnbitstudio.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dnbitstudio.spotifystreamer.models.CustomTrack;
import com.dnbitstudio.spotifystreamer.services.PlayTrackResultReceiver;
import com.dnbitstudio.spotifystreamer.services.PlayTrackService;
import com.dnbitstudio.spotifystreamer.services.PlayTrackService.PlayTrackBinder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayTrackActivityFragment extends DialogFragment
        implements PlayTrackResultReceiver.Receiver
{
    private final String LOG_TAG = this.getClass().getSimpleName();
    public static final String TRACKS = "tracks_key";
    public static final String TRACK_NUMBER = "track_number_key";
    public static final String TRACK_SHARE_INTENT = " #Spotify Streamer";
    public static final String PLAY_TRACK_FRAGMENT_TAG = "PTF_TAG";

    @Bind(R.id.play_track_artist_and_album)
    TextView artistAndAlbum;
    @Bind(R.id.play_track_artwork)
    ImageView artwork;
    @Bind(R.id.play_track_name)
    TextView trackName;
    @Bind(R.id.play_track_seekbar)
    SeekBar seekBar;
    @Bind(R.id.play_track_current_time)
    TextView currentTime;
    @Bind(R.id.play_track_total_time)
    TextView totalTime;
    @BindBool(R.bool.sw600)
    boolean mTwoPane;
    private ImageButton playPauseButton;

    private final Handler mHandler = new Handler();
    private PlayTrackService playTrackService;
    private Intent playIntent;
    private ShareActionProvider shareActionProvider;
    private boolean restored = false;
    public PlayTrackResultReceiver playTrackResultReceiver;
    private int duration;
    private Runnable seekbarRunnable;

    public PlayTrackActivityFragment()
    {
        setHasOptionsMenu(true);
    }

    public static PlayTrackActivityFragment newInstance(ArrayList<CustomTrack> customTracks, int position)
    {
        PlayTrackActivityFragment fragment = new PlayTrackActivityFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(TRACKS, customTracks);
        args.putInt(TRACK_NUMBER, position);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootview = inflater.inflate(R.layout.fragment_play_track, container, false);
        ButterKnife.bind(this, rootview);

        playPauseButton = (ImageButton) rootview.findViewById(R.id.bt_media_play_pause);

        if (savedInstanceState != null)
        {
            restored = true;
        }

        // Initialize the custom result receiver
        playTrackResultReceiver = new PlayTrackResultReceiver(new Handler());
        playTrackResultReceiver.setReceiver(this);

        return rootview;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.common_share_action_provider, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // if it is 2 Pane this is already shown
        if (mTwoPane)
        {
            menuItem.setVisible(false);
        }
        // Get the provider to a field
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (playIntent == null)
        {
            playIntent = new Intent(getActivity(), PlayTrackService.class);
            playIntent.putExtra(PlayTrackService.ARGS_BUNDLE, getArguments());
            playIntent.putExtra(PlayTrackService.RECEIVER_TAG, playTrackResultReceiver);

            // When rotating the service is already started
            if (!restored)
            {
                getActivity().startService(playIntent);
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().bindService(playIntent, playTrackConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getActivity().unbindService(playTrackConnection);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Call the superclass to remove the dialog title
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @OnClick(R.id.bt_media_previous)
    public void playPrevious(ImageButton button)
    {
        playTrackService.playPrevious();
    }

    @OnClick(R.id.bt_media_play_pause)
    public void playPause(ImageButton button)
    {
        String drawable = (String) button.getTag();
        if (drawable.equals("play"))
        {
            togglePlay();
            togglePlayButton();
        } else if (drawable.equals("pause"))
        {
            togglePause();
            togglePauseButton();
        }
    }

    public void togglePause()
    {
        // pause track and stop runnable
        playTrackService.pauseTrack();
        mHandler.removeCallbacks(seekbarRunnable);
    }

    public void togglePauseButton()
    {
        // update the play-pause button
        playPauseButton.setTag("play");
        playPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    public void togglePlay()
    {
        // restart track and post runnable
        playTrackService.restartTrack();
        mHandler.post(seekbarRunnable);
    }

    public void togglePlayButton()
    {
        // update the play-pause button
        playPauseButton.setTag("pause");
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    @OnClick(R.id.bt_media_next)
    public void playNext(ImageButton button)
    {
        playTrackService.playNext();
    }

    public void launchMediaPlayer()
    {
        updateTrackDetails();

        seekbarRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                updateSeekbarAndCurrentTime();

                if (!playTrackService.isTrackCompleted())
                {
                    mHandler.postDelayed(this, 1000);
                }
            }
        };

        // Update Seekbar on UI thread
        // only if the track is been played
        if (playTrackService.isPlaying())
        {
            getActivity().runOnUiThread(seekbarRunnable);
        }
        updateSeekbarAndCurrentTime();
    }

    public void updateTrackDetails()
    {
        // Attach an intent to this ShareActionProvider.
        if (shareActionProvider != null)
        {
            shareActionProvider.setShareIntent(createShareTrackIntent());
        }

        long millis = playTrackService.getDuration();

        duration = (int) Math.round(millis / 1000.0);

        seekBar.setMax(duration);

        totalTime.setText(setSecondsString(duration));

        CustomTrack track = playTrackService.getTracks().get(playTrackService.getTrackNumber());
        artistAndAlbum.setText(track.getArtist() + "\n" + track.getAlbum());
        Picasso.with(getActivity())
                .load(track.getUrl_large())
                .placeholder(R.drawable.ic_loading)
                .error(R.mipmap.ic_launcher)
                .into(artwork);

        trackName.setText(track.getName());
    }

    public void updateSeekbarAndCurrentTime()
    {
        if (!playTrackService.isTrackCompleted())
        {
            int seekbarPosition = Math.round(playTrackService.getPosition() / 1000);
            seekBar.setProgress(seekbarPosition);
            currentTime.setText(setSecondsString(seekbarPosition));
        } else
        {
            seekBar.setProgress(duration);
            currentTime.setText(setSecondsString(duration));
        }
    }

    public String setSecondsString(int duration)
    {
        if (duration < 10)
        {
            return "0:0" + duration;
        } else
        {
            return "0:" + duration;
        }
    }

    private Intent createShareTrackIntent()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //noinspection deprecation
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        CustomTrack track = playTrackService.getTracks().get(playTrackService.getTrackNumber());

        shareIntent.putExtra(Intent.EXTRA_TEXT, track.getPreview_url() + TRACK_SHARE_INTENT);
        return shareIntent;
    }

    private final ServiceConnection playTrackConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            PlayTrackBinder binder = (PlayTrackBinder) service;
            //get service
            playTrackService = binder.getService();

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                private boolean wasPlaying = false;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                    wasPlaying = playTrackService.isPlaying();
                    playTrackService.pause();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                    int progress = seekBar.getProgress();
                    playTrackService.seekTo(progress * 1000);
                    if (wasPlaying)
                    {
                        playTrackService.start();
                    }
                    currentTime.setText(setSecondsString(progress));
                }
            });

            if (playTrackService.isTrackCompleted())
            {
                togglePlayButton();
            }

            // if it is not restored and it is not same track
            // we play a new track
            if (!restored && !playTrackService.isSameTrack())
            {
                playTrackService.playNewTrack();
            } else
            {
                if (playTrackService.isSameTrack() && !playTrackService.isPlaying())
                {
                    togglePauseButton();
                }

                launchMediaPlayer();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        if (resultCode == PlayTrackService.NOTIFY_MP_PREPARED)
        {
            launchMediaPlayer();
        }

        if (resultCode == PlayTrackService.NOTIFY_TRACK_COMPLETED)
        {
            mHandler.removeCallbacks(seekbarRunnable);
            playPauseButton.setTag("play");
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
            updateSeekbarAndCurrentTime();
        }
    }
}