package com.example.memorableplacesii;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);



        ArrayList<String> addresses = new ArrayList<>();
        addresses.add("add new place...");


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,addresses);
        listView.setAdapter(arrayAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(),i+"",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("Places",i);
                startActivity(intent);
            }
        });
        Intent intent = getIntent();
        ArrayList<String> savedPlaces = (ArrayList<String>) getIntent().getSerializableExtra("savedPlaces");
        if(savedPlaces != null)
            addresses.addAll(savedPlaces);

    }
}