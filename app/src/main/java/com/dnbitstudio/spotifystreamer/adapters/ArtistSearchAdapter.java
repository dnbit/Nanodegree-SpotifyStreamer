package com.dnbitstudio.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dnbitstudio.spotifystreamer.R;
import com.dnbitstudio.spotifystreamer.models.CustomArtist;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ArtistSearchAdapter extends ArrayAdapter<CustomArtist>
{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Context context;
    private final int layoutResource;
    private final ArrayList<CustomArtist> values;

    private final LayoutInflater inflater;
    public static final int DEFAULT_THUMBNAIL = R.mipmap.ic_launcher;

    public ArtistSearchAdapter(Context context, int layoutResource, ArrayList<CustomArtist> values)
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
            holder.image = (ImageView) convertView.findViewById(R.id.list_item_artist_search_thumbnail);
            holder.text = (TextView) convertView.findViewById(R.id.list_item_artist_search_name);

            convertView.setTag(holder);
        } else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        CustomArtist customArtist = values.get(position);

        holder.text.setText(customArtist.getName());

        if (!customArtist.getUrl().equals(""))
        {
            Picasso.with(context).load(customArtist.getUrl()).into(holder.image);
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
