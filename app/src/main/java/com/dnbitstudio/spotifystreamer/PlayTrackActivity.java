package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.dnbitstudio.spotifystreamer.models.CustomTrack;

import java.util.ArrayList;


public class PlayTrackActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_track);

        Intent intent = getIntent();
        if (savedInstanceState == null && intent != null)
        {
            // Only reachable by startIntent from TopTracksActivity
            ArrayList<CustomTrack> customTracks = intent.getParcelableArrayListExtra(PlayTrackActivityFragment.TRACKS);
            int position = intent.getIntExtra(PlayTrackActivityFragment.TRACK_NUMBER, 0);
            PlayTrackActivityFragment playTrackFragment =
                    PlayTrackActivityFragment.newInstance(customTracks, position);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.play_track_container, playTrackFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_track, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
