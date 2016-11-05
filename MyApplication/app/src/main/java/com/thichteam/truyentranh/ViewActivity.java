package com.thichteam.truyentranh;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.thichteam.truyentranh.common.GlobalConst;
import com.thichteam.truyentranh.common.PicassoImageLoader;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.loader.DefaultImageLoader;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewActivity extends FragmentActivity {

    private ScrollGalleryView scrollGalleryView;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        id = getIntent().getExtras().getString(GlobalConst.EXTRA_CHAPTER_ID);

        scrollGalleryView = (ScrollGalleryView) findViewById(R.id.scroll_gallery_view);
        View thumbnailContainer = scrollGalleryView.findViewById(R.id.thumbnails_container);
        thumbnailContainer.setVisibility(View.INVISIBLE);
        buildImageList();
    }

    private void buildImageList() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = GlobalConst.API_BASE_URL + "chap/detail?id=" + id;

        final FragmentActivity that = this;
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseJSONObj) {
                        // Display the first 500 characters of the response string.
                        try {
                            if (responseJSONObj.getBoolean("success")) {
                                JSONObject chap = responseJSONObj.getJSONObject("data");
                                JSONArray imageArray = chap.getJSONArray("images");
                                if (imageArray.length() == 0) {
                                    Toast.makeText(that, "No more data to display!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                List<MediaInfo> infos = new ArrayList<>(imageArray.length());
                                for (int i = 0; i < imageArray.length(); i++) {
                                    infos.add(MediaInfo.mediaLoader(new PicassoImageLoader(imageArray.optString(i))));
                                }
                                scrollGalleryView
                                        .setThumbnailSize(100)
                                        .setZoom(true)
                                        .setFragmentManager(getSupportFragmentManager())
                                        .addMedia(infos);
                            } else {
                                GlobalConst.showErrorDialog(that);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(that, "Parse JSON error!", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(that, "Network problem!", Toast.LENGTH_LONG).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }
}
