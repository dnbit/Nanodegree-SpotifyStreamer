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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
    private static final String ADAPTER_KEY = "adapter";
    public static final String ARTIST_ID = "artistID";

    private TopTracksAdapter adapter;
    private String artistID = "";

    public TopTracksActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.getStringExtra(ARTIST_ID) != null)
        {
            artistID = intent.getStringExtra(ARTIST_ID);
        }

        if (savedInstanceState != null)
        {
            adapter = savedInstanceState.getParcelable(ADAPTER_KEY);
        }
        else
        {
            adapter = new TopTracksAdapter(getActivity(),
                    R.layout.list_item_top_tracks,
                    new ArrayList<Track>());
        }

        ListView listView = (ListView) rootView.findViewById(R.id.listview_top_artist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String message = adapter.getItem(position).name;
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });

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
        outState.putParcelable(ADAPTER_KEY, adapter);
    }

    public void performSearch(String artistID)
    {
        if (CommonHelper.isNetworkConnected(getActivity()))
        {
            new FetchTopTracksTask().execute(artistID);
        } else
        {
            Toast.makeText(getActivity(), getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, List<Track>>
    {
        public static final int MIN_IMAGE_SIZE_SMALL = 200;

        @Override
        protected List<Track> doInBackground(String... params)
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

            Tracks tracks = spotify.getArtistTopTrack(params[0], map);

            if (tracks != null && tracks.tracks != null
                    && tracks.tracks.size() > 0)
            {
                List<Track> actualTracks = tracks.tracks;
                for (Track track : actualTracks)
                {
                    if (track.album != null && track.album.images != null
                            && track.album.images.size() > 0)
                    {
                        List<Image> images = track.album.images;

                        ListIterator iterator = images.listIterator(images.size());
                        // We want the smallest with width >= 200
                        // We remove smaller than this to ensure it is
                        // always at size()-1
                        while (iterator.hasPrevious())
                        {
                            Image image = (Image) iterator.previous();
                            if (iterator.hasPrevious())
                            {
                                if (image.width < MIN_IMAGE_SIZE_SMALL)
                                {
                                    iterator.remove();
                                } else
                                {
                                    break;
                                }
                            }
                        }
                        // Cache images
                        Picasso.with(getActivity()).load(images.get(images.size() - 1).url).fetch();
                    }
                }
                return actualTracks;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Track> results)
        {
            if (results != null && results.size() > 0)
            {
                adapter.clear();
                // Probably unnecessary but just in case
                for (int i = 0; i < 10; i++)
                {
                    adapter.add(results.get(i));
                }
            }
        }
    }
}
