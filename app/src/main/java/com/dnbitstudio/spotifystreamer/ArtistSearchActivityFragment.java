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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    private ArtistSearchAdapter adapter;
    private SearchView searchView;
    private ListView listView;

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
                new ArrayList<Artist>());

        listView = (ListView) rootView.findViewById(R.id.listview_artist_search);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String artistID = adapter.getItem(position).id;
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(TopTracksActivityFragment.ARTIST_ID, artistID);

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
    public boolean onQueryTextSubmit(String artistName)
    {
        if (artistName.length() < 1)
        {
            adapter.clear();
            listView.setSelectionAfterHeaderView();
        } else
        {
            performSearch(artistName);
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

    public class FetchArtistSearchTask extends AsyncTask<String, Void, List<Artist>>
    {

        public static final int MIN_IMAGE_SIZE_SMALL = 200;

        @Override
        protected List<Artist> doInBackground(String... params)
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
                for (Artist artist : artists)
                {
                    List<Image> images = artist.images;
                    if (images.size() > 0)
                    {
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
                return artists;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Artist> results)
        {
            if (results != null && results.size() > 0)
            {
                searchView.clearFocus();
                adapter.clear();
                for (Artist artist : results)
                {
                    adapter.add(artist);
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
