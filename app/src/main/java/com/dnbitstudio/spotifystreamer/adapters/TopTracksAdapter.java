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

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TopTracksAdapter extends ArrayAdapter<CustomTrack>
{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Context context;
    private final int layoutResource;
    private final ArrayList<CustomTrack> values;

    private final LayoutInflater inflater;

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

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        CustomTrack customTrack = values.get(position);

        holder.text.setText(customTrack.getName() + "\n" + customTrack.getAlbum());

        if (customTrack.getUrl() != null)
        {
            Picasso.with(context)
                    .load(customTrack.getUrl())
                    .placeholder(R.drawable.ic_loading)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.image);
        } else
        {
            // default thumbnail
            Picasso.with(context).load(R.mipmap.ic_launcher).into(holder.image);
        }
        return convertView;
    }

    static class ViewHolder
    {
        @InjectView(R.id.list_item_top_tracks_thumbnail)
        ImageView image;
        @InjectView(R.id.list_item_top_tracks_track_name)
        TextView text;

        public ViewHolder(View view)
        {
            ButterKnife.inject(this, view);
        }
    }
}
