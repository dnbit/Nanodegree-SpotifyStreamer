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
import java.util.Locale;

import kaaes.spotify.webapi.android.models.Track;

public class TopTracksAdapter extends ArrayAdapter<Track>
{
    private final Context context;
    private final int layoutResource;
    private final ArrayList<Track> values;

    public TopTracksAdapter(Context context, int layoutResource, ArrayList<Track> values) {
        super(context, layoutResource, values);
        this.context = context;
        this.layoutResource = layoutResource;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(layoutResource, parent, false);
        ImageView thumbnail = (ImageView) rowView.findViewById(R.id.list_item_top_tracks_thumbnail);
        TextView trackName = (TextView) rowView.findViewById(R.id.list_item_top_tracks_track_name);

        trackName.setText(values.get(position).name + "\n" + values.get(position).album.name);
        if(values.get(position).album.images.size() > 0) {
            Picasso.with(context).load(values.get(position).album.images.get(0).url).into(thumbnail);
        }
        return rowView;
    }
}
