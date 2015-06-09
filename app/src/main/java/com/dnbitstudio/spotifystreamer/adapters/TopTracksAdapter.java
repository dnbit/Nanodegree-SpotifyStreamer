package com.dnbitstudio.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dnbitstudio.spotifystreamer.R;
import com.dnbitstudio.spotifystreamer.models.CustomTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TopTracksAdapter extends ArrayAdapter<CustomTrack>
{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Context context;
    private final int layoutResource;
    private final ArrayList<CustomTrack> values;

    private final LayoutInflater inflater;
    public static final int DEFAULT_THUMBNAIL = R.mipmap.ic_launcher;

    public TopTracksAdapter(Context context, int layoutResource, ArrayList<CustomTrack> values)
    {
        super(context, layoutResource, values);
        this.context = context;
        this.layoutResource = layoutResource;
        this.values = values;
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // ViewHolder pattern
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(layoutResource, null);

            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.list_item_top_tracks_thumbnail);
            holder.text = (TextView) convertView.findViewById(R.id.list_item_top_tracks_track_name);

            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        CustomTrack customTrack = values.get(position);

        holder.text.setText(customTrack.getName() + "\n" + customTrack.getAlbum());

        if (!customTrack.getUrl().equals(""))
        {
            Picasso.with(context).load(customTrack.getUrl()).into(holder.image);
        } else
        {
            // default thumbnail
            Picasso.with(context).load(DEFAULT_THUMBNAIL).into(holder.image);
        }
        return convertView;
    }

    private static class ViewHolder
    {
        public ImageView image;
        public TextView text;
    }
}
