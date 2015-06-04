package com.dnbitstudio.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dnbitstudio.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistSearchAdapter extends ArrayAdapter<Artist>
{
    private final Context context;
    private final ArrayList<Artist> values;

    public ArtistSearchAdapter(Context context, int layout, int view, ArrayList<Artist> values) {
        super(context, layout, view, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_artist_search, parent, false);
        TextView artistName = (TextView) rowView.findViewById(R.id.list_item_artist_search_name);
        ImageView thumbnail = (ImageView) rowView.findViewById(R.id.list_item_artist_search_thumbnail);

        artistName.setText(values.get(position).name);
        if(values.get(position).images.size() > 0) {
            Picasso.with(context).load(values.get(position).images.get(0).url).into(thumbnail);
        }
        return rowView;
    }
}
