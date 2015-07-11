package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class TopTracksActivity extends AppCompatActivity
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
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
