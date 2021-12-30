package com.example.memorableplacesii;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    static ArrayList<String> places;
    static  ArrayList<LatLng> locations;
    static ArrayAdapter arrayAdapter;
    SharedPreferences sharedPreferences;
    ArrayList<String> longitudes;
    ArrayList<String> latitudes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        places = new ArrayList<>();
        locations = new ArrayList<>();
        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();

        sharedPreferences = this.getSharedPreferences("com.example.memorableplacesii",MODE_PRIVATE);

        // clear data
        locations.clear();
        latitudes.clear();
        longitudes.clear();
        places.clear();


        try {
            places = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("lats",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("lons",ObjectSerializer.serialize(new ArrayList<String>())));

            Log.i("places",ObjectSerializer.serialize(MainActivity.places));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if(places.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0)
        {
            if(places.size() == latitudes.size() && places.size() == longitudes.size())
            {
                for(int i=0;i<places.size();i++)
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
            }
        }
        else
        {
            places.add("add new place...");
            locations.add(new LatLng(0,0));
        }

        arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, places);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("placeNumber",i);
                startActivity(intent);
            }
        });
    }
}