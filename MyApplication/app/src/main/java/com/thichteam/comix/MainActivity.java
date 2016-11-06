package com.thichteam.comix;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.thichteam.comix.adapter.ComixAdapter;
import com.thichteam.comix.common.GlobalConst;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ComixAdapter adapter;
    private List<JSONObject> comixs;
    private boolean isLoading = false;
    private int currentPage = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        final AppCompatActivity that =this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(that, SearchActivity.class);
                intent.putExtra(GlobalConst.EXTRA_QUERY, query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        listView = (ListView) findViewById(R.id.comix_list_view);

        comixs = new ArrayList<>();
        adapter = new ComixAdapter(this, comixs);

        listView.setAdapter(adapter);
        final AppCompatActivity that = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(that, DetailActivity.class);
                intent.putExtra(GlobalConst.EXTRA_COMIX_ID, comixs.get(position).optString("url"));
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

        buildDataSet();
    }

    private void buildDataSet() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = GlobalConst.API_BASE_URL + "all?page=" + (++currentPage);

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
                                    Toast.makeText(that, "No more data to display!", Toast.LENGTH_LONG).show();
                                    --currentPage;
                                    return;
                                }
                                for (int i = 0; i < comixArray.length(); i++) {
                                    comixs.add(comixArray.getJSONObject(i));
                                }
                                adapter.notifyDataSetChanged();
//                                Toast.makeText(that, "Loading page " + currentPage + " successful!", Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("Error", responseJSONObj.toString());
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
}
