package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dnbitstudio.spotifystreamer.models.CustomTrack;

import java.util.ArrayList;

public class TopTracksActivity extends AppCompatActivity implements TopTracksActivityFragment.TopTracksFragmentCallback
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        Intent intent = getIntent();
        if (intent != null)
        {
            String artistName = intent.getStringExtra(TopTracksActivityFragment.ARTIST_NAME);
            if (getSupportActionBar() != null)
            {
                getSupportActionBar().setSubtitle(artistName);
            }
        }
        if (savedInstanceState == null && intent != null)
        {
            // Only reachable by startIntent from ArtistSearchActivity
            String artistID = intent.getStringExtra(TopTracksActivityFragment.ARTIST_ID);
            String artistName = intent.getStringExtra(TopTracksActivityFragment.ARTIST_NAME);
            TopTracksActivityFragment topTracksFragment =
                    TopTracksActivityFragment.newInstance(artistID, artistName);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, topTracksFragment).commit();
        }
    }

    @Override
    public void onItemSelected(ArrayList<CustomTrack> customTracks, int position)
    {
        // Note that this will be only called if it is single Pane
        //Create intent and start new activity
        Intent intent = new Intent(this, PlayTrackActivity.class);
        intent.putParcelableArrayListExtra(PlayTrackActivityFragment.TRACKS, customTracks);
        intent.putExtra(PlayTrackActivityFragment.TRACK_NUMBER, position);

        startActivity(intent);
    }
}
