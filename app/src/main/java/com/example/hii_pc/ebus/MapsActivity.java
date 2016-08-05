package com.example.hii_pc.ebus;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.hii_pc.ebus.booking.SeatActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, Listener {

    private static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 1;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        Log.e("UHOH", "PLS3");
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap != null) {
            // Try to obtain the map from the SupportMapFragment.
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_LOCATION);
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.e("UHOH", "UHOH PERMISSING MISSING");
                return;
            }
            Log.d("CALLED", "CALLED");
            mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    List<Marker> markers = new ArrayList<Marker>();

                    @Override
                    public void onMyLocationChange(Location arg0) {
                        mMap.clear();
                        LatLng myPos = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                        Marker me = mMap.addMarker(new MarkerOptions().position(myPos).title("Hello Its me"));
                        markers.add(me);

                        LatLng Bus = new LatLng(arg0.getLatitude() - 0.001, arg0.getLongitude() - 0.001);
                        Marker busMarker = mMap.addMarker(new MarkerOptions()
                                .position(Bus)
                                .title("BUS")
                                .snippet("HELLO BUS")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
                        markers.add(busMarker);

                        LatLng Bus2 = new LatLng(arg0.getLatitude() + 0.05, arg0.getLongitude() + 0.05);
                        Marker bus2Marker = mMap.addMarker(new MarkerOptions()
                                .position(Bus2)
                                .title("BUS")
                                .snippet("HELLO BUS")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
                        markers.add(bus2Marker);
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));
                        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos,18.0f));
                        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (Marker marker : markers) {
                            builder.include(marker.getPosition());
                        }
                        LatLngBounds bounds = builder.build();

                        int padding = 100;
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        mMap.animateCamera(cu);

                        // Checks, whether start and end locations are captured
                        if (markers.size() >= 2) {
                            LatLng origin = markers.get(0).getPosition();
                            LatLng dest = markers.get(2).getPosition();

                            // Getting URL to the Google Directions API
                            String url = GetDataFromUrl.getDirectionsUrl(origin, dest);
                            GetDirections getDirections = new GetDirections(MapsActivity.this);
                            getDirections.startGettingDirections(url);
                        }

                    }
                });

            }
        }
    }

    //The task for getting directions ends up here...
    @Override
    public void onSuccessfulRouteFetch(final List<List<HashMap<String, String>>> result) {
        Log.e("HUH", "SUCCESS!");

        //if it takes a long time, we will do it in a seperate thread...
        new Thread(new Runnable() {
            public PolylineOptions lineOptions;
            public ArrayList<LatLng> points;

            @Override
            public void run() {

                MarkerOptions markerOptions = new MarkerOptions();
                // Traversing through all the routes
                for (List<HashMap<String, String>> path : result) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    int size = path.size();
                    // Get all the points for this route
                    for (HashMap<String, String> point : path) {
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(12);
                    lineOptions.color(Color.RED);
                }

                //Do all UI operations on the UI thread only...
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Drawing polyline in the Google Map for the this route
                        mMap.addPolyline(lineOptions);
                    }
                });

            }
        }).start();

    }

    @Override
    public void onFail() {
        Log.e("HUH", "FAILED!");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpMapIfNeeded();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Log.e("PERMISSION", "PERMISSION DENIED");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.seat:
                Intent myIntent = new Intent(this, SeatActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.misc:
                //TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
