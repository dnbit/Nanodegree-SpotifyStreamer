package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dnbitstudio.spotifystreamer.adapters.ArtistSearchAdapter;
import com.dnbitstudio.spotifystreamer.models.CustomArtist;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A fragment for the artist search
 */
public class ArtistSearchActivityFragment extends Fragment
        implements android.support.v7.widget.SearchView.OnQueryTextListener,
        android.support.v7.widget.SearchView.OnCloseListener
{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final String CUSTOM_ARTISTS_KEY = "custom_artists_key";
    private static final String IS_ASYNC_TASK_RUNNING = "is_async_task_running_key";
    private static final String ARTIST_QUERY = "artist_query";
    private ArtistSearchAdapter adapter;
    private SearchView searchView;
    private ListView listView;
    private ProgressBar progressBar;

    // variables to manage rotation
    private ArrayList<CustomArtist> customArtists = new ArrayList<>();
    private boolean isQueryRunning = false;
    private String artistQuery;

    public static final int MIN_IMAGE_SIZE_SMALL = 200;

    public ArtistSearchActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        // Set the searchView
        searchView = (SearchView) rootView.findViewById(R.id.search_artist_name);
        searchView.setQueryHint(getString(R.string.searchview_hint));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        // Initialize the adapter
        adapter = new ArtistSearchAdapter(getActivity(),
                R.layout.list_item_artist_search,
                new ArrayList<CustomArtist>());

        // get saved values and update adapter
        if (savedInstanceState != null)
        {
            isQueryRunning = savedInstanceState.getBoolean(IS_ASYNC_TASK_RUNNING);
            customArtists = savedInstanceState.getParcelableArrayList(CUSTOM_ARTISTS_KEY);
            artistQuery = savedInstanceState.getString(ARTIST_QUERY);
            if (customArtists != null)
            {
                updateAdapter();
            }
        }

        listView = (ListView) rootView.findViewById(R.id.listview_artist_search);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String artistID = adapter.getItem(position).getId();
                String artistName = adapter.getItem(position).getName();

                //Create intent and start new activity
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(TopTracksActivityFragment.ARTIST_ID, artistID);
                intent.putExtra(TopTracksActivityFragment.ARTIST_NAME, artistName);

                startActivity(intent);
            }
        });

        progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress_bar);

        // Cache default img
        // Note: It is safe to invoke fetch from any thread
        Picasso.with(getActivity()).load(R.mipmap.ic_launcher).fetch();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        // if query did not finish run it again
        if (isQueryRunning)
        {
            performSearch(artistQuery);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CUSTOM_ARTISTS_KEY, customArtists);
        outState.putBoolean(IS_ASYNC_TASK_RUNNING, isQueryRunning);
        outState.putString(ARTIST_QUERY, artistQuery);
    }

    @Override
    public boolean onQueryTextSubmit(String s)
    {
        // save value in appropriate variable
        artistQuery = s;
        if (artistQuery.length() < 1)
        {
            adapter.clear();
            listView.setSelectionAfterHeaderView();
        } else
        {
            performSearch(artistQuery);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s)
    {
        return false;
    }

    @Override
    public boolean onClose()
    {
        return false;
    }

    public void performSearch(String artist)
    {
        if (CommonHelper.isNetworkConnected(getActivity()))
        {
            // The query is now running
            isQueryRunning = true;

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            spotify.searchArtists(artist, new Callback<ArtistsPager>()
            {
                @Override
                public void success(ArtistsPager artistsPager, Response response)
                {
                    if (artistsPager != null && artistsPager.artists != null &&
                            artistsPager.artists.items != null &&
                            artistsPager.artists.items.size() > 0)
                    {
                        parseData(artistsPager);
                    } else
                    {
                        String message = getString(R.string.refine_search);
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    }
                    // Query has now finished
                    isQueryRunning = false;

                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            updateUIAfterSearch();
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error)
                {
                    // Query has now finished
                    isQueryRunning = false;

                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String message = getString(R.string.connection_error);
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else
        {
            isQueryRunning = false;
            Toast.makeText(getActivity(), getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    public void parseData(ArtistsPager artistsPager)
    {
        List<Artist> artists = artistsPager.artists.items;

        // Risk of rotation while creating our
        // model's objects force to use a temporary variable
        ArrayList<CustomArtist> tempArtists = new ArrayList<>();
        CustomArtist customArtist;
        for (Artist artist : artists)
        {
            List<Image> images = artist.images;
            String url = CommonHelper.getImageURL(images, MIN_IMAGE_SIZE_SMALL);

            customArtist =
                    new CustomArtist(artist.name, url, artist.id);
            tempArtists.add(customArtist);
        }
        // once our model's objects are created
        // we can assign it to the appropriate variable
        customArtists = tempArtists;
    }

    public void updateUIAfterSearch()
    {
        toggleVisibility();
        searchView.clearFocus();
        updateAdapter();
        listView.setSelectionAfterHeaderView();
    }

    public void toggleVisibility()
    {
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }

    private void updateAdapter()
    {
        adapter.clear();
        for (CustomArtist customArtist : customArtists)
        {
            adapter.add(customArtist);
        }
    }
}