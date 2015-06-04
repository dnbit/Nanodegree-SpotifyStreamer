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

import com.dnbitstudio.spotifystreamer.adapters.ArtistSearchAdapter;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchActivityFragment extends Fragment {

    public ArtistSearchAdapter mArtistSearchAdapter;

    public ArtistSearchActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchArtistSearchTask searchTask = new FetchArtistSearchTask();
        searchTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mArtistSearchAdapter = new ArtistSearchAdapter(getActivity(),
                R.layout.list_item_artist_search,
                R.id.list_item_artist_search_rootview,
                new ArrayList<Artist>());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_artist_search);
        listView.setAdapter(mArtistSearchAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.list_item_artist_search_name);

                String artist = tv.getText().toString();
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra("artist", artist);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchArtistSearchTask extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            return spotify.searchArtists("adele");
        }

        @Override
        protected void onPostExecute(ArtistsPager results) {
            if (results != null)
            {
                mArtistSearchAdapter.clear();
                for (Artist artist : results.artists.items) {
                    mArtistSearchAdapter.add(artist);
                }
            }
        }
    }
}
