package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
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


/**
 * A fragment for the artist search
 */
public class ArtistSearchActivityFragment extends Fragment implements android.support.v7.widget.SearchView.OnQueryTextListener,
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
    private boolean isAsyncTaskRunning = false;
    private String artistQuery;

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
            isAsyncTaskRunning = savedInstanceState.getBoolean(IS_ASYNC_TASK_RUNNING);
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
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        // if Async Task did not finish run it again
        if (isAsyncTaskRunning)
        {
            performSearch(artistQuery);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        // Cache default img
        // Note: It is safe to invoke fetch from any thread
        Picasso.with(getActivity()).load(ArtistSearchAdapter.DEFAULT_THUMBNAIL).fetch();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CUSTOM_ARTISTS_KEY, customArtists);
        outState.putBoolean(IS_ASYNC_TASK_RUNNING, isAsyncTaskRunning);
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
            // The Async Task is now running
            isAsyncTaskRunning = true;
            new FetchArtistSearchTask().execute(artist);
        } else
        {
            isAsyncTaskRunning = false;
            Toast.makeText(getActivity(), getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAdapter()
    {
        adapter.clear();
        for (CustomArtist customArtist : customArtists)
        {
            adapter.add(customArtist);
        }
    }

    public class FetchArtistSearchTask extends AsyncTask<String, Void, ArrayList<CustomArtist>>
    {
        public static final int MIN_IMAGE_SIZE_SMALL = 200;
        private boolean retrofitError = false;

        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected ArrayList<CustomArtist> doInBackground(String... params)
        {
            if (params == null || params.length == 0)
            {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            ArtistsPager artistsPager;

            // Need to catch Runtime exception by retrofit timeout
            try
            {
                artistsPager = spotify.searchArtists(params[0]);
            } catch (RuntimeException ex)
            {
                retrofitError = true;
                return null;
            }

            if (artistsPager != null && artistsPager.artists != null &&
                    artistsPager.artists.items != null &&
                    artistsPager.artists.items.size() > 0)
            {
                List<Artist> artists = artistsPager.artists.items;

                // Risk of rotation while creating our
                // model's objects force to use a temporary variable
                ArrayList<CustomArtist> tempArtists = new ArrayList<>();
                CustomArtist customArtist;
                for (Artist artist : artists)
                {
                    List<Image> images = artist.images;
                    String url = CommonHelper.getImageURL(getActivity(), images, MIN_IMAGE_SIZE_SMALL);

                    // Cache images
                    if (url.length() > 0)
                    {
                        Picasso.with(getActivity()).load(url).fetch();
                    }

                    customArtist =
                            new CustomArtist(artist.name, url, artist.id);
                    tempArtists.add(customArtist);
                }
                // once our model's objects are created
                // we can assign it to the appropriate variable
                customArtists = tempArtists;

                // AsyncTask has now finished
                isAsyncTaskRunning = false;
                return customArtists;
            }

            // AsyncTask has now finished
            isAsyncTaskRunning = false;
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<CustomArtist> results)
        {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (results != null)
            {
                searchView.clearFocus();
                updateAdapter();
                listView.setSelectionAfterHeaderView();
            } else
            {
                String message = getString(R.string.refine_search);
                if (retrofitError)
                {
                    message = getString(R.string.connection_error);
                    retrofitError = false;
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }
}