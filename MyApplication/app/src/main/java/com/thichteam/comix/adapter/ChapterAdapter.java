package com.thichteam.comix.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.thichteam.comix.R;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by chanhlt on 05/11/2016.
 */
public class ChapterAdapter extends ArrayAdapter<JSONObject> {

    private Context context;
    private List<JSONObject> chaps;

    public ChapterAdapter(Context context, List<JSONObject> chaps) {
        super(context, R.layout.list_view_item_chapter, chaps);
        this.context = context;
        this.chaps = chaps;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listViewItem = inflater.inflate(R.layout.list_view_item_chapter, parent, false);
        JSONObject chap = chaps.get(position);

        TextView chapId = (TextView) listViewItem.findViewById(R.id.chapter_id);
        chapId.setText(chap.optString("id"));
        TextView chapName = (TextView) listViewItem.findViewById(R.id.chapter_name);
        chapName.setText(chap.optString("name"));

        return listViewItem;
    }
}
