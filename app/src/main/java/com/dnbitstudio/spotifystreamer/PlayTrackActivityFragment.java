package com.dnbitstudio.spotifystreamer;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
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
    public final String TRACK_SHARE_INTENT = " #Spotify Streamer";

    String preview_url;

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

    private MediaPlayer mediaPlayer;
    private ArrayList<CustomTrack> tracks;
    private int trackNumber;
    private CustomTrack track;
    private Handler mHandler = new Handler();

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

        Bundle args = getArguments();
        if (args != null)
        {
            tracks = args.getParcelableArrayList(TRACKS);
            trackNumber = args.getInt(TRACK_NUMBER);

            if (tracks.get(trackNumber) != null)
            {
                track = tracks.get(trackNumber);
                preview_url = track.getPreview_url();
                updateTrackDetails();
            }
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (mediaPlayer != null && fromUser)
                {
                    mediaPlayer.seekTo(progress * 1000);
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
        return rootview;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_play_track_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.
        if (shareActionProvider != null)
        {
            shareActionProvider.setShareIntent(createShareTrackIntent());
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

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
        launchMediaPlayer();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mediaPlayer.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("trackNumber", trackNumber);
    }

    @OnClick(R.id.bt_media_previous)
    public void playPrevious(ImageButton button)
    {
        mediaPlayer.stop();
        if (trackNumber == 0)
        {
            trackNumber = tracks.size() - 1;
        } else
        {
            trackNumber = --trackNumber;
        }
        preview_url = tracks.get(trackNumber).getPreview_url();
        launchMediaPlayer();
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Call the superclass to remove the dialog title
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @OnClick(R.id.bt_media_play_pause)
    public void playPause(ImageButton button)
    {
        String drawable = (String) button.getTag();
        if (drawable.equals("play"))
        {
            button.setTag("pause");
            button.setImageResource(android.R.drawable.ic_media_pause);
            mediaPlayer.start();
        } else if (drawable.equals("pause"))
        {
            button.setTag("play");
            button.setImageResource(android.R.drawable.ic_media_play);
            mediaPlayer.pause();
        }
    }

    @OnClick(R.id.bt_media_next)
    public void playNext(ImageButton button)
    {
        mediaPlayer.stop();
        if (trackNumber == tracks.size() - 1)
        {
            trackNumber = 0;
        } else
        {
            trackNumber = ++trackNumber;
        }
        preview_url = tracks.get(trackNumber).getPreview_url();
        launchMediaPlayer();
    }

    public void launchMediaPlayer()
    {
        track = tracks.get(trackNumber);
        try
        {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(preview_url);
            mediaPlayer.prepare();

            long millis = mediaPlayer.getDuration();
            int seconds = (int) Math.round(millis / 1000.0);
            seekBar.setMax(seconds);

            totalTime.setText(setSecondsString(seconds));
            mediaPlayer.start();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        updateTrackDetails();


        //Make sure you update Seekbar on UI thread
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (mediaPlayer != null)
                {
                    int seekbarPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(seekbarPosition);
                    currentTime.setText(setSecondsString(seekbarPosition));
                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }

    public void updateTrackDetails()
    {
        artistAndAlbum.setText(track.getArtist() + "\n" + track.getAlbum());
        Picasso.with(getActivity())
                .load(track.getUrl_large())
                .placeholder(R.drawable.ic_loading)
                .error(R.mipmap.ic_launcher)
                .into(artwork);
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, track.getPreview_url() + TRACK_SHARE_INTENT);
        return shareIntent;
    }
}