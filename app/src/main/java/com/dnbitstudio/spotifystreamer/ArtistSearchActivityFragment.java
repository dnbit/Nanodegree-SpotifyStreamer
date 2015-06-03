package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchActivityFragment extends Fragment {

    public ArtistSearchActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        List<String> values = new ArrayList<>();

        // TODO remove fake data
        int count = 1;
        for (int i = 0; i < 33; i++) {
            values.add("" + count++);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_artist_search,
                R.id.list_item_artist_search_textview,
                values);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_artist_search);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.list_item_artist_search_textview);

                String artist = tv.getText().toString();
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra("artist", artist);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
