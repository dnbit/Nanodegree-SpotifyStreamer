package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dnbitstudio.spotifystreamer.adapters.TopTracksAdapter;
import com.dnbitstudio.spotifystreamer.models.CustomTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A fragment for the top tracks
 */
public class TopTracksActivityFragment extends Fragment
{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final String CUSTOM_TRACKS_KEY = "custom_tracks_key";
    private static final String IS_QUERY_RUNNING = "is_query_running_key";
    public static final String ARTIST_ID = "artistID";
    public static final String ARTIST_NAME = "artistName";
    public static final String IS_SHARE_VISIBLE = "isSharedVisible";
    public static final String IS_NOW_PLAYING_VISIBLE = "is_now_playing";
    public static final String POSITION = "position_key";
    String artistName;

    private TopTracksAdapter adapter;
    private ShareActionProvider shareActionProvider;

    @Bind(R.id.listview_top_artist)
    ListView listView;
    @Bind(R.id.search_progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.no_results)
    TextView tvNoResults;
    @BindBool(R.bool.sw600)
    boolean mTwoPane;

    // variables to manage rotation
    private ArrayList<CustomTrack> customTracks;
    private boolean isQueryRunning = false;

    public static final int MIN_IMAGE_SIZE_SMALL = 200;
    public static final int MIN_IMAGE_SIZE_LARGE = 640;
    private boolean isShareVisible = false;
    private boolean isNowPlayingVisible = false;
    private MenuItem menuNowPlaying;
    private int position;

    public TopTracksActivityFragment()
    {
        setHasOptionsMenu(true);
    }

    public static TopTracksActivityFragment newInstance(String artistID, String artistName)
    {
        TopTracksActivityFragment fragment = new TopTracksActivityFragment();

        Bundle args = new Bundle();
        args.putString(ARTIST_ID, artistID);
        args.putString(ARTIST_NAME, artistName);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        ButterKnife.bind(this, rootView);

        // Initialize the adapter
        adapter = new TopTracksAdapter(getActivity(),
                R.layout.list_item_top_tracks,
                new ArrayList<CustomTrack>());

        // get saved values and update adapter
        if (savedInstanceState != null)
        {
            isQueryRunning = savedInstanceState.getBoolean(IS_QUERY_RUNNING);
            customTracks = savedInstanceState.getParcelableArrayList(CUSTOM_TRACKS_KEY);
            if (customTracks != null && customTracks.size() > 0)
            {
                updateUIAfterSearch();
            } else
            {
                toggleVisibility();
            }

            isShareVisible = savedInstanceState.getBoolean(IS_SHARE_VISIBLE);
            isNowPlayingVisible = savedInstanceState.getBoolean(IS_NOW_PLAYING_VISIBLE);
            position = savedInstanceState.getInt(POSITION);
        }

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                setPosition(position);
                isNowPlayingVisible = true;
                if (mTwoPane)
                {
                    isShareVisible = true;
                    // Attach an intent to this ShareActionProvider.
                    if (shareActionProvider != null)
                    {
                        shareActionProvider.
                                setShareIntent(createShareTrackIntent(customTracks, position));
                    }
                    ((ArtistSearchActivity) getActivity()).onItemSelected(customTracks, position);
                } else
                {
                    ((TopTracksActivity) getActivity()).onItemSelected(customTracks, position);
                }
            }
        });

        Bundle args = getArguments();
        if (args != null)
        {
            artistName = args.getString(ARTIST_NAME);
            String artistID = args.getString(ARTIST_ID);
            if (savedInstanceState == null || isQueryRunning)
            {
                performSearch(artistID);
            }
        }

        // Cache default img
        // Note: It is safe to invoke fetch from any thread
        Picasso.with(getActivity()).load(R.mipmap.ic_launcher).fetch();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.common_now_playing, menu);
        inflater.inflate(R.menu.common_share_action_provider, menu);

        // Retrieve the share menu item
        MenuItem menuShareItem = menu.findItem(R.id.action_share);
        menuNowPlaying = menu.findItem(R.id.now_playing);

        if (!isShareVisible)
        {
            menuShareItem.setVisible(false);
        }

        // make it visible on tablet layout
        if (isNowPlayingVisible)
        {
            menuNowPlaying.setVisible(true);
        }

        // Get the provider to a field
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuShareItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.now_playing)
        {
            setNowPlaying();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // make it visible on phone layout
        if (isNowPlayingVisible && menuNowPlaying != null)
        {
            menuNowPlaying.setVisible(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CUSTOM_TRACKS_KEY, customTracks);
        outState.putBoolean(IS_QUERY_RUNNING, isQueryRunning);
        outState.putBoolean(IS_SHARE_VISIBLE, isShareVisible);
        outState.putBoolean(IS_NOW_PLAYING_VISIBLE, isNowPlayingVisible);
        outState.putInt(POSITION, position);
    }

    public void performSearch(String artistID)
    {
        if (CommonHelper.isNetworkConnected(getActivity()))
        {
            // The query is now running
            isQueryRunning = true;

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> map = new HashMap<>();
            /*String country = Locale.getDefault().getCountry();
            if (country.equals(""))
            {
                country = "US";
            }*/

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String country = sharedPreferences.getString(getString(R.string.pref_country_key), getString(R.string.pref_country_default));

            map.put("country", country);

            spotify.getArtistTopTrack(artistID, map, new Callback<Tracks>()
            {
                @Override
                public void success(Tracks tracks, Response response)
                {
                    if (tracks != null && tracks.tracks != null
                            && tracks.tracks.size() > 0)
                    {
                        parseData(tracks);

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
                    } else
                    {
                        // Query has now finished
                        isQueryRunning = false;

                        getActivity().runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                toggleVisibility();
                            }
                        });
                    }
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
                            toggleVisibility();
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

    public void parseData(Tracks tracks)
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
                String url_small = CommonHelper.getImageURL(images, MIN_IMAGE_SIZE_SMALL);
                String url_large = CommonHelper.getImageURL(images, MIN_IMAGE_SIZE_LARGE);

                customTrack =
                        new CustomTrack(track.name, track.album.name,
                                url_small, url_large, track.preview_url, artistName);
                tempTracks.add(customTrack);
            }
        }

        // once our model's objects are created
        // we can assign it to the appropriate variable
        customTracks = tempTracks;
    }

    public void updateUIAfterSearch()
    {
        toggleVisibility();
        updateAdapter();
    }

    public void toggleVisibility()
    {
        progressBar.setVisibility(View.GONE);
        if (customTracks == null || customTracks.size() == 0)
        {
            tvNoResults.setVisibility(View.VISIBLE);
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

    private Intent createShareTrackIntent(List<CustomTrack> customTracks, int position)
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //noinspection deprecation
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        CustomTrack track = customTracks.get(position);

        shareIntent.putExtra(Intent.EXTRA_TEXT, track.getPreview_url() + PlayTrackActivityFragment.TRACK_SHARE_INTENT);
        return shareIntent;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    private void setNowPlaying()
    {
        if (mTwoPane)
        {
            ((ArtistSearchActivity) getActivity()).onItemSelected(customTracks, position);
        } else
        {
            ((TopTracksActivity) getActivity()).onItemSelected(customTracks, position);
        }
    }

    public interface TopTracksFragmentCallback
    {
        void onItemSelected(ArrayList<CustomTrack> customTracks, int position);
    }
}
