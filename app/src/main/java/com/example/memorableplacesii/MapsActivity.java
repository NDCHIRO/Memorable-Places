package com.example.memorableplacesii;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.CaseMap;
import android.icu.text.DecimalFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    ArrayList<String> savePlaces;
    Map<Double,Double> allLatLong;
    int placesClickedCount = 0;

    public void centerMapOnLocation(Location location, String title)
    {
        mMap.clear();
        LatLng clickedLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(clickedLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clickedLocation, 12));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 5, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerMapOnLocation(lastKnownLocation, "Your Location");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        savePlaces = new ArrayList<>();
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        // if we got number zero from the Main activity so add new place button is clicked
        if(intent.getIntExtra("placeNumber", 0) == 0)
        {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    centerMapOnLocation(location,"Your location");
                    /*LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your current location"));*/
                }
            };

            if (Build.VERSION.SDK_INT < 23) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 5, locationListener);
            }
            else
                {
                //if request not granted yet
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                //if request is granted, get the last known location for the user
                else {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 5, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerMapOnLocation(lastKnownLocation,"Your Location");
                }
            }
        }

        // if any button clicked from the ListView except 0, it will show the place and its name
        else
        {
            Location clickedLocation = new Location(LocationManager.GPS_PROVIDER);
            clickedLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).latitude);
            clickedLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).longitude);
            centerMapOnLocation(clickedLocation,MainActivity.places.get(intent.getIntExtra("placeNumber", 0)));
        }
    }
    // when user long click on a location, app gets (if any of the following exits) street  name, locality, governorate.
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        String address = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (listAddresses != null && listAddresses.size() > 0)
            {
                if (listAddresses.get(0).getThoroughfare() != null)
                    address += listAddresses.get(0).getThoroughfare() + " ";

                if (listAddresses.get(0).getLocality() != null)
                    address += listAddresses.get(0).getLocality() + " ";

                if (listAddresses.get(0).getAdminArea() != null)
                    address += listAddresses.get(0).getAdminArea();

                // if the road is unnamed just replace it with the date
                if(address.contains("Unnamed"))
                    address = setDateFormat();

            }
            // if the result is not found at all  just replace it with the date
            else
                address = setDateFormat();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        //add the places and location their lists
        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        //notify the arrayAdapter to update
        MainActivity.arrayAdapter.notifyDataSetChanged();

        Toast.makeText(MapsActivity.this, address, Toast.LENGTH_SHORT).show();
        Toast.makeText(MapsActivity.this, "location saved!!", Toast.LENGTH_SHORT).show();
    }


    // when the road is not found just replace it with the date
    public String setDateFormat()
    {
        String address="";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
        address += sdf.format(new Date());
        return address;
    }

}


