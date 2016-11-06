package com.thichteam.comix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.thichteam.comix.adapter.ChapterAdapter;
import com.thichteam.comix.common.GlobalConst;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private ListView listView;
    private ChapterAdapter adapter;
    private List<JSONObject> chaps;
    private boolean isLoading = false;
    private int currentPage = 0;
    private String id;

    private TextView comixName;
    private TextView comixAuthor;
    private TextView comixCategory;
    private TextView comixStatus;
    private TextView comixChapNumber;
    private TextView chapListLabel;
    private TextView comixShortContent;
    private NetworkImageView comixThumbnail;
    private ScrollView mainScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        id = getIntent().getExtras().getString(GlobalConst.EXTRA_COMIX_ID);

        chaps = new ArrayList<>();
        adapter = new ChapterAdapter(this, chaps);
        listView = (ListView) findViewById(R.id.chapter_list_view);
        listView.setAdapter(adapter);

        comixName = (TextView) findViewById(R.id.comix_name);
        comixAuthor = (TextView) findViewById(R.id.comix_author);
        comixCategory = (TextView) findViewById(R.id.comix_category);
        comixChapNumber = (TextView) findViewById(R.id.comix_chap_number);
        comixStatus = (TextView) findViewById(R.id.comix_status);
        comixThumbnail = (NetworkImageView) findViewById(R.id.comix_thumbnail);
        chapListLabel = (TextView) findViewById(R.id.chap_list_label);
        comixShortContent = (TextView) findViewById(R.id.comix_short_content);
        mainScrollView = (ScrollView) findViewById(R.id.main_scroll_view);

        buildComixInfo();
        buildDataSet();

        final AppCompatActivity that = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(that, ViewActivity.class);
                intent.putExtra(GlobalConst.EXTRA_CHAPTER_ID, chaps.get(position).optString("url"));
                startActivity(intent);
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    if (isLoading == false) {
                        isLoading = true;
                        buildDataSet();
                    }
                }
            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

    }

    private void buildDataSet() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = GlobalConst.API_BASE_URL + "chap/all?id=" + id + "&page=" + (++currentPage);

        final AppCompatActivity that = this;
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseJSONObj) {
                        // Display the first 500 characters of the response string.
                        try {
                            if (responseJSONObj.getBoolean("success")) {
                                JSONArray comixArray = responseJSONObj.getJSONArray("data");
                                if (comixArray.length() == 0) {
//                                    Toast.makeText(that, "No more data to display!", Toast.LENGTH_LONG).show();
                                    --currentPage;
                                    return;
                                }
                                for (int i = 0; i < comixArray.length(); i++) {
                                    chaps.add(comixArray.getJSONObject(i));
                                }
                                adapter.notifyDataSetChanged();
//                                Toast.makeText(that, "Loading page " + currentPage + " successful!", Toast.LENGTH_LONG).show();
                                GlobalConst.setListViewHeightBasedOnChildren(that, listView);
                                mainScrollView.scrollTo(0,0);
                            } else {
                                GlobalConst.showErrorDialog(that);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(that, "Parse JSON error!", Toast.LENGTH_LONG).show();
                        }
                        isLoading = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(that, "Network problem!", Toast.LENGTH_LONG).show();
                isLoading = false;
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }

    private void buildComixInfo() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = GlobalConst.API_BASE_URL + "detail?id=" + id;

        final AppCompatActivity that = this;
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseJSONObj) {
                        // Display the first 500 characters of the response string.
                        try {
                            if (responseJSONObj.getBoolean("success")) {
                                JSONObject comix = responseJSONObj.getJSONObject("data");
                                ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                                comixThumbnail.setImageUrl(comix.optString("thumbnail"), imageLoader);
                                comixName.setText(comix.optString("name"));
                                comixAuthor.setText(comix.optString("author"));
                                comixCategory.setText(comix.optString("type"));
                                comixStatus.setText(comix.optString("status"));
                                comixChapNumber.setText(comix.optString("chapNumber"));
                                comixShortContent.setText(comix.optString("shortContent"));
                                chapListLabel.setText(R.string.chapter_list);
                            } else {
                                Log.e("Error", "url: " + url + ", respose:" + responseJSONObj.toString());
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
