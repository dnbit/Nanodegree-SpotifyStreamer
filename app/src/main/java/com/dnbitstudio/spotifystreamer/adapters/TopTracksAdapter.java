package com.dnbitstudio.spotifystreamer.adapters;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dnbitstudio.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TopTracksAdapter extends ArrayAdapter<Track> implements Parcelable
{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Context context;
    private final int layoutResource;
    private final ArrayList<Track> values;

    private final LayoutInflater inflater;
    public static final int DEFAULT_THUMBNAIL = R.mipmap.ic_launcher;

    public TopTracksAdapter(Context context, int layoutResource, ArrayList<Track> values)
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

        holder.text.setText(values.get(position).name + "\n" + values.get(position).album.name);

        List<Image> images = values.get(position).album.images;
        int listSize = images.size();
        if (listSize > 0)
        {
            // we have removed the too small ones
            // and the latest is the smallest
            // so we just need the latest
            Picasso.with(context).load(images.get(listSize - 1).url).into(holder.image);
        } else
        {
            // default thumbnail
            Picasso.with(context).load(DEFAULT_THUMBNAIL).into(holder.image);
        }
        return convertView;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {

    }

    private static class ViewHolder
    {
        public ImageView image;
        public TextView text;
    }
}
