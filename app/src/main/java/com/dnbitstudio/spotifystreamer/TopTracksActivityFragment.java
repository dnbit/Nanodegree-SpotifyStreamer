package com.dnbitstudio.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    public TopTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        String artist = "";
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.getStringExtra("artist") != null){
            artist = intent.getStringExtra("artist");
        }

        List<String> values = new ArrayList<>();

        // TODO remove fake data
        int count = 1;
        for (int i = 0; i < 10; i++) {
            values.add(artist + "." + count++);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_top_tracks,
                R.id.list_item_top_tracks_textview,
                values);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_top_artist);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view.findViewById(R.id.list_item_top_tracks_textview);

                String message = tv.getText().toString();
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
