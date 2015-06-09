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
    private ArtistSearchAdapter adapter;
    private SearchView searchView;
    private ListView listView;

    private ArrayList<CustomArtist> customArtists;

    public ArtistSearchActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        searchView = (SearchView) rootView.findViewById(R.id.search_artist_name);
        searchView.setQueryHint(getString(R.string.searchview_hint));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        adapter = new ArtistSearchAdapter(getActivity(),
                R.layout.list_item_artist_search,
                new ArrayList<CustomArtist>());

        if(savedInstanceState != null)
        {
            customArtists = savedInstanceState.getParcelableArrayList(CUSTOM_ARTISTS_KEY);
            for(CustomArtist customArtist: customArtists)
            {
                adapter.add(customArtist);
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
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(TopTracksActivityFragment.ARTIST_ID, artistID);
                intent.putExtra(TopTracksActivityFragment.ARTIST_NAME, artistName);

                startActivity(intent);
            }
        });
        return rootView;
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
    }

    @Override
    public boolean onQueryTextSubmit(String artistQuery)
    {
        if (artistQuery.length() < 1)
        {
            adapter.clear();
            listView.setSelectionAfterHeaderView();
        } else
        {
            performSearch(artistQuery);
        }
        return true;
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
            new FetchArtistSearchTask().execute(artist);
        } else
        {
            Toast.makeText(getActivity(), getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    public class FetchArtistSearchTask extends AsyncTask<String, Void, ArrayList<CustomArtist>>
    {

        public static final int MIN_IMAGE_SIZE_SMALL = 200;

        @Override
        protected ArrayList<CustomArtist> doInBackground(String... params)
        {
            if (params == null || params.length == 0)
            {
                return null;
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            ArtistsPager artistsPager = spotify.searchArtists(params[0]);

            if (artistsPager != null && artistsPager.artists != null &&
                    artistsPager.artists.items != null)
            {
                List<Artist> artists = artistsPager.artists.items;
                customArtists = new ArrayList<>();
                for (Artist artist : artists)
                {
                    List<Image> images = artist.images;
                    String url = CommonHelper.getImageURL(getActivity(), images, MIN_IMAGE_SIZE_SMALL);

                    CustomArtist customArtist =
                            new CustomArtist(artist.name, url, artist.id);
                    customArtists.add(customArtist);
                }
                return customArtists;
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<CustomArtist> results)
        {
            if (results != null && results.size() > 0)
            {
                searchView.clearFocus();
                adapter.clear();
                for (CustomArtist customArtist : results)
                {
                    adapter.add(customArtist);
                }
                listView.setSelectionAfterHeaderView();
            } else
            {
                String message = getString(R.string.refine_search);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
