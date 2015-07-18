package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.dnbitstudio.spotifystreamer.models.CustomTrack;

import java.util.ArrayList;

public class ArtistSearchActivity extends AppCompatActivity
        implements ArtistSearchActivityFragment.ArtistSearchFragmentCallback,
        TopTracksActivityFragment.TopTracksFragmentCallback
{
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        // if it takes the boolean from sw600 it is two pane
        mTwoPane = getResources().getBoolean(R.bool.sw600);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String artistID, String artistName)
    {
        if (mTwoPane)
        {
            TopTracksActivityFragment topTracksFragment =
                    TopTracksActivityFragment.newInstance(artistID, artistName);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, topTracksFragment).commit();
        } else
        {
            //Create intent and start new activity
            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.putExtra(TopTracksActivityFragment.ARTIST_ID, artistID);
            intent.putExtra(TopTracksActivityFragment.ARTIST_NAME, artistName);

            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(ArrayList<CustomTrack> customTracks, int position)
    {
        // Note that this will be only called from
        // if it is 2 Pane
        PlayTrackActivityFragment newFragment = PlayTrackActivityFragment.newInstance(customTracks, position);
        newFragment.show(getSupportFragmentManager(), PlayTrackActivityFragment.PLAY_TRACK_FRAGMENT_TAG);
    }
}