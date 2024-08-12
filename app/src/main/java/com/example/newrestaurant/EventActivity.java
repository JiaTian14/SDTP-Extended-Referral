package com.example.newrestaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EventActivity extends AppCompatActivity {

    private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        eventListView = findViewById(R.id.eventListView);

        // fetch events from API
        loadStaticEvents();
    }
    private void loadStaticEvents() {
        List<String> events = new ArrayList<>();
        events.add("Name: AI Talk\nDate: 2024-08-15\nDetails: A talk about AI smart home.\n");
        events.add("Name: Fun Run\nDate: 2024-08-20\nDetails: A outdoor run around the campus.\n");
        events.add("Name: Food Festival\nDate: 2024-08-25\nDetails: Selling food and some station game.\n");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(EventActivity.this, android.R.layout.simple_list_item_1, events);
        eventListView.setAdapter(adapter);
    }


    private void fetchEvents(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("http://192.168.1.113:8080/shipcampus/get_event.php");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        reader.close();

                        final List<String> events = parseEvents(stringBuilder.toString());


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(EventActivity.this, android.R.layout.simple_list_item_1, events);
                                eventListView.setAdapter(adapter);
                            }
                        });

                        connection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    private List<String> parseEvents(String jsonResponse) {
        List<String> events = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject eventObject = jsonArray.getJSONObject(i);
                String eventName = eventObject.getString("event_name");
                events.add(eventName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return events;
    }
}