package com.dealfaro.luca.listviewexample;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "lv-ex";

    RequestQueue queue;

    private class ListElement {
        ListElement() {};

        ListElement(String title, String subtitle, String url) {
            titleLabel = title;
            subtitleLabel = subtitle;
            urlLabel = url;
        }

        public String titleLabel, subtitleLabel, urlLabel;
    }

    private ArrayList<ListElement> aList;

    private class MyAdapter extends ArrayAdapter<ListElement> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                LayoutInflater vi = (LayoutInflater)
                        getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
            TextView subtitleView = (TextView) newView.findViewById(R.id.subtitleView);
            tv.setText(w.titleLabel);
            subtitleView.setText(w.subtitleLabel);

            // Set a listener for the whole list item.
            newView.setTag(w.urlLabel);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = v.getTag().toString();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, s, duration);
                    toast.show();

                    Intent intent = new Intent(context, ReaderActivity.class);
                    intent.putExtra("URL", s);
                    startActivity(intent);
                }
            });

            return newView;
        }
    }

    private MyAdapter aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this); // queue for JSON reponse
        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();
        View v = (View) findViewById(R.id.refreshButton);
        clickRefresh(v);
    }

    public void clickRefresh (View v) {
        Log.i(LOG_TAG, "Requested a refresh of the list");
        Random rn = new Random();

        String get_url = "https://luca-ucsc-teaching-backend.appspot.com/hw4/get_news_sites";

        aList.clear();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, get_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray news_site_arr = response.getJSONArray("news_sites");
                            String title, subtitle, url;
                            for (int i = 0; i < news_site_arr.length(); i++) {
                                JSONObject c = news_site_arr.getJSONObject(i);
                                title = c.getString("title");
                                subtitle = c.getString("subtitle");
                                url = c.getString("url");
                                aList.add(new ListElement(title, subtitle, url));
                            }

                            aa.notifyDataSetChanged();
                        } catch (JSONException e) {
                            //my_textView.setText("Received bad json: " + e.getStackTrace());
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d(LOG_TAG, error.toString());
                    }
                });

        queue.add(jsObjRequest);
    }

}
