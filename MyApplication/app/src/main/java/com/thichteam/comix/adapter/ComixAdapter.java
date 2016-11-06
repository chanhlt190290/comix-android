package com.thichteam.comix.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.thichteam.comix.AppController;
import com.thichteam.comix.R;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by chanhlt on 05/11/2016.
 */
public class ComixAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    private List<JSONObject> comixs;

    public ComixAdapter(Context context, List<JSONObject> comixs) {
        super(context, R.layout.list_view_item_comix, comixs);
        this.context = context;
        this.comixs = comixs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listViewItem = inflater.inflate(R.layout.list_view_item_comix, parent, false);
        JSONObject comix = comixs.get(position);

        NetworkImageView comixThumbnail = (NetworkImageView) listViewItem.findViewById(R.id.comix_thumbnail);
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        comixThumbnail.setImageUrl(comix.optString("thumbnail"), imageLoader);

        TextView comixName = (TextView) listViewItem.findViewById(R.id.comix_name);
        comixName.setText(comix.optString("name"));
        TextView comixAuthor = (TextView) listViewItem.findViewById(R.id.comix_author);
        comixAuthor.setText(comix.optString("author"));
        TextView comixCategory = (TextView) listViewItem.findViewById(R.id.comix_category);
        comixCategory.setText(comix.optString("type"));

        return listViewItem;
    }
}
