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
import android.util.Log;
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
import com.dnbitstudio.spotifystreamer.services.PlayTrackService;
import com.dnbitstudio.spotifystreamer.services.PlayTrackService.PlayTrackBinder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayTrackActivityFragment extends DialogFragment
{
    private final String LOG_TAG = this.getClass().getSimpleName();
    public static final String TRACKS = "tracks_key";
    public static final String TRACK_NUMBER = "track_number_key";
    public static final String TRACK_SHARE_INTENT = " #Spotify Streamer";
    public static final String PLAY_TRACK_FRAGMENT_TAG = "PTF_TAG";

    @InjectView(R.id.play_track_artist_and_album)
    TextView artistAndAlbum;
    @InjectView(R.id.play_track_artwork)
    ImageView artwork;
    @InjectView(R.id.play_track_name)
    TextView trackName;
    @InjectView(R.id.play_track_seekbar)
    SeekBar seekBar;
    @InjectView(R.id.play_track_current_time)
    TextView currentTime;
    @InjectView(R.id.play_track_total_time)
    TextView totalTime;

    //
    //private int trackNumber;
    //private CustomTrack track;
    private final Handler mHandler = new Handler();
    private PlayTrackService playTrackService;
    private Intent playIntent;
    private ShareActionProvider shareActionProvider;
    private boolean rotated = false;

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
        ButterKnife.inject(this, rootview);

        if (savedInstanceState != null)
        {
            rotated = true;
        }

        return rootview;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_play_track_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

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

            if (!rotated)
            {
                playIntent.putExtra(PlayTrackService.ARGS_BUNDLE, getArguments());
                getActivity().startService(playIntent);
            }
        }

        // Set the Layout dimensions to be WRAP_CONTENT
        // to avoid the layout to be shrunk
        // only if we are using the fragment as a dialog
        if (getDialog() != null)
        {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        outState.putInt("trackNumber", playTrackService.getTrackNumber());
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
        updateTrackDetails();
    }

    @OnClick(R.id.bt_media_play_pause)
    public void playPause(ImageButton button)
    {
        String drawable = (String) button.getTag();
        if (drawable.equals("play"))
        {
            button.setTag("pause");
            button.setImageResource(android.R.drawable.ic_media_pause);
            playTrackService.restartTrack();
        } else if (drawable.equals("pause"))
        {
            button.setTag("play");
            button.setImageResource(android.R.drawable.ic_media_play);
            playTrackService.pauseTrack();
        }
    }

    @OnClick(R.id.bt_media_next)
    public void playNext(ImageButton button)
    {
        playTrackService.playNext();
        updateTrackDetails();
    }

    public void launchMediaPlayer()
    {

        //Make sure you update Seekbar on UI thread
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                long millis = playTrackService.getMediaPlayer().getDuration();
                // TODO this should not be live after song finished
                Log.i(LOG_TAG, "**ACTIVITYFRAGMENT-ONSERVICECONNECTED** Duration = " + millis);
                int seconds = (int) Math.round(millis / 1000.0);
                seekBar.setMax(seconds);

                totalTime.setText(setSecondsString(seconds));
                if (playTrackService.getMediaPlayer() != null)
                {
                    int seekbarPosition = playTrackService.getMediaPlayer().getCurrentPosition() / 1000;
                    seekBar.setProgress(seekbarPosition);
                    currentTime.setText(setSecondsString(seekbarPosition));
                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    public void updateTrackDetails()
    {

        // Attach an intent to this ShareActionProvider.
        if (shareActionProvider != null)
        {
            shareActionProvider.setShareIntent(createShareTrackIntent());
        }

        CustomTrack track = playTrackService.getTracks().get(playTrackService.getTrackNumber());
        artistAndAlbum.setText(track.getArtist() + "\n" + track.getAlbum());
        Picasso.with(getActivity())
                .load(track.getUrl_large())
                .placeholder(R.drawable.ic_loading)
                .error(R.mipmap.ic_launcher)
                .into(artwork, new Callback()
                {
                    @Override
                    public void onSuccess()
                    {
                        // TODO check this callbacks
                    }

                    @Override
                    public void onError()
                    {

                    }
                });
        trackName.setText(track.getName());
    }

    public String setSecondsString(int seconds)
    {
        if (seconds < 10)
        {
            return "0:0" + seconds;
        } else
        {
            return "0:" + seconds;
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
            updateTrackDetails();

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    if (playTrackService.getMediaPlayer() != null && fromUser)
                    {
                        playTrackService.getMediaPlayer().seekTo(progress * 1000);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {

                }
            });

            launchMediaPlayer();
            if (!rotated)
            {
                playTrackService.playTrack();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }
    };
}