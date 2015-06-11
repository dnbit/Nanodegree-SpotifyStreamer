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
import android.widget.Toast;

import com.dnbitstudio.spotifystreamer.adapters.TopTracksAdapter;
import com.dnbitstudio.spotifystreamer.models.CustomTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A fragment for the top tracks
 */
public class TopTracksActivityFragment extends Fragment
{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final String CUSTOM_TRACKS_KEY = "custom_tracks_key";
    private static final String IS_ASYNC_TASK_RUNNING = "is_async_task_running_key";
    public static final String ARTIST_ID = "artistID";
    public static final String ARTIST_NAME = "artistName";

    private TopTracksAdapter adapter;
    private String artistID = "";

    // variables to manage rotation
    private ArrayList<CustomTrack> customTracks;
    private boolean isAsyncTaskRunning = false;

    public TopTracksActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // Initialize the adapter
        adapter = new TopTracksAdapter(getActivity(),
                R.layout.list_item_top_tracks,
                new ArrayList<CustomTrack>());

        // get saved values and update adapter
        if(savedInstanceState != null)
        {
            isAsyncTaskRunning = savedInstanceState.getBoolean(IS_ASYNC_TASK_RUNNING);
            customTracks = savedInstanceState.getParcelableArrayList(CUSTOM_TRACKS_KEY);
            updateAdapter();
        }

        ListView listView = (ListView) rootView.findViewById(R.id.listview_top_artist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String message = adapter.getItem(position).getName();
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getActivity().getIntent();
        if (intent != null)
        {
            artistID = intent.getStringExtra(ARTIST_ID);
        }

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        performSearch(artistID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CUSTOM_TRACKS_KEY, customTracks);
        outState.putBoolean(IS_ASYNC_TASK_RUNNING, isAsyncTaskRunning);
    }

    public void performSearch(String artistID)
    {
        if (CommonHelper.isNetworkConnected(getActivity()))
        {
            // The Async Task is now running
            isAsyncTaskRunning = true;
            new FetchTopTracksTask().execute(artistID);
        } else
        {
            isAsyncTaskRunning = false;
            Toast.makeText(getActivity(), getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAdapter()
    {
        adapter.clear();
        for (CustomTrack customTrack : customTracks)
        {
            adapter.add(customTrack);
        }
    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, ArrayList<CustomTrack>>
    {
        public static final int MIN_IMAGE_SIZE_SMALL = 200;
        private boolean retrofitError = false;

        @Override
        protected ArrayList<CustomTrack> doInBackground(String... params)
        {

            if (params == null || params.length == 0)
            {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> map = new HashMap<>();
            String country = Locale.getDefault().getCountry();
            if (country.equals(""))
            {
                country = "US";
            }
            map.put("country", country);

            Tracks tracks;
            try
            {
                tracks = spotify.getArtistTopTrack(params[0], map);
            } catch (RuntimeException ex)
            {
                retrofitError = true;
                return null;
            }

            if (tracks != null && tracks.tracks != null
                    && tracks.tracks.size() > 0)
            {
                List<Track> actualTracks = tracks.tracks;

                // Risk of rotation while creating our
                // model's objects force to use a temporary variable
                ArrayList<CustomTrack> tempTracks = new ArrayList<>();
                CustomTrack customTrack;
                // Probably unnecessary but just in case
                for (int i = 0; i < actualTracks.size() && i < 10; i++)
                {
                    Track track = actualTracks.get(i);
                    if (track.album != null && track.album.images != null
                            && track.album.images.size() > 0)
                    {
                        List<Image> images = track.album.images;
                        String url = CommonHelper.getImageURL(images, MIN_IMAGE_SIZE_SMALL);

                        // Cache images
                        if (url.length() > 0)
                        {
                            Picasso.with(getActivity()).load(url).fetch();
                        }

                        customTrack =
                                new CustomTrack(track.name, track.album.name,
                                        url, track.id);
                        tempTracks.add(customTrack);
                    }
                }
                // once our model's objects are created
                // we can assign it to the appropriate variable
                customTracks = tempTracks;

                // AsyncTask has now finished
                isAsyncTaskRunning = false;
                return customTracks;
            }
            // AsyncTask has now finished
            isAsyncTaskRunning = false;
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<CustomTrack> results)
        {
            if (results != null && results.size() > 0)
            {
                adapter.clear();
                updateAdapter();
            }
        }
    }
}
