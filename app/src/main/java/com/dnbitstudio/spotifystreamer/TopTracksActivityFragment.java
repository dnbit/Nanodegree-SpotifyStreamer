package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dnbitstudio.spotifystreamer.adapters.TopTracksAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    private final String LOG_TAG = this.getClass().getSimpleName();
    public static final String ARTIST_ID = "artistID";
    public String artistID="";

    public TopTracksAdapter mTopTracksAdapter;

    public TopTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.getStringExtra(ARTIST_ID) != null){
            artistID = intent.getStringExtra(ARTIST_ID);
        }

        mTopTracksAdapter = new TopTracksAdapter(getActivity(),
                R.layout.list_item_top_tracks,
                new ArrayList<Track>());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_top_artist);
        listView.setAdapter(mTopTracksAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = mTopTracksAdapter.getItem(position).name;
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        new FetchTopTracksTask().execute(artistID);
    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, Tracks>
    {

        @Override
        protected Tracks doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> map = new HashMap<>();
            String country = Locale.getDefault().getCountry();
            if (country.equals(""))
            {
                country = "US";
            }

            map.put("country", country);
            return spotify.getArtistTopTrack(params[0], map);
        }

        @Override
        protected void onPostExecute(Tracks results) {
            if (results != null && results.tracks.size() > 0)
            {
                mTopTracksAdapter.clear();
                for (Track track : results.tracks)
                {
                    mTopTracksAdapter.add(track);
                }
            }
        }
    }
}
