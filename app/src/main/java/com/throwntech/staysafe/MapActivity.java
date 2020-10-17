package com.throwntech.staysafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import static com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import static com.throwntech.staysafe.R.drawable.ic_red_alert;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_PHONE_CALL = 1;
    LocationManager lm;


    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private HashMap<Long, Marker> markerHashMap;

    //Accessing CardView from map_activity.xml
    private CardView cardView;

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void displayNotification(String area, double latitude, double longitude) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(ic_red_alert).setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.icon)).setContentTitle("Warning")
                .setContentText("Leopard detected near " + area).setColor(1255082051).setPriority(NotificationCompat.PRIORITY_DEFAULT);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MapActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: going to institute's location");
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapActivity.this, "Grant location permission for better functionality.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
        final LatLng latLng = new LatLng(23.1767917, 80.0236891);
        float zoom = 15f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (mLocationPermissionGranted) {
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.d(TAG, "onComplete: got the location");
                        Location currentLocation = (Location) task.getResult();
                        assert currentLocation != null;
                        final double latmax = 23.187010;
                        final double latmin = 23.164480;
                        final double lngmax = 80.040768;
                        final double lngmin = 80.008818;
                        final double currLat = currentLocation.getLatitude();
                        final double currLng = currentLocation.getLongitude();
                        if (currLat >= latmin && currLat <= latmin && currLng >= lngmax && currLng >= lngmin) {
                            final LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            float zoom = 15f;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                        }
                        if ((ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                            mMap.setMyLocationEnabled(true);
                        }

                    }
                    else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(MapActivity.this, "Couldn't find the current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
//        try {
//            if (mLocationPermissionGranted) {
//                Task location = mFusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "onComplete: got the location");
//                            Location currentLocation = (Location) task.getResult();
//                            assert currentLocation != null;
//                            final LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                            float zoom = 15f;
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
//                            mMap.setMyLocationEnabled(true);
//                        }
//                        else {
//                            Log.d(TAG, "onComplete: current location is null");
//                            Toast.makeText(MapActivity.this, "Couldn't find the current location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        } catch (SecurityException e) {
//            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
//        }
//    }

    private void markerAdder(long id, String level, double longitude, double latitude) {
        if (!markerHashMap.containsKey(id)) {
            Marker marker;
            String str = Long.toString(id);
            if (level.equals("1")) {
                Drawable alert = getResources().getDrawable(ic_red_alert);
                BitmapDescriptor markerIcon = getMarkerIconFromDrawable(alert);
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(longitude, latitude)).title("Camera ID: " + str).icon(markerIcon));
                markerHashMap.put(id, marker);
            }
        } else {
            Marker marker = markerHashMap.get(id);
            assert marker != null;
            if (level.equals("0")) {
                marker.remove();
                cardView.setVisibility(View.INVISIBLE);
                markerHashMap.remove(id);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready here");
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick: Map clicked");
                cardView.setVisibility(View.GONE);
            }
        });
        addingInitFirebaseData(mMap);
        addingFirebaseData(mMap);
        getDeviceLocation();

        //Checkpoint Markers, i.e., the location of the Security Checkpoints in IIITDM Jabalpur
        Drawable alert = getResources().getDrawable(R.drawable.ic_checkpost);
        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(alert);
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.179882, 80.026603)).icon(markerIcon).title("Main Gate"));//Gate
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.179194, 80.022584)).icon(markerIcon).title("Nescafé"));//Nescafe
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.177254, 80.019757)).icon(markerIcon).title("Behind Hall 4"));//Hall3-4
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.174870, 80.020809)).icon(markerIcon).title("Hall 7"));//Hall 7
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.176032, 80.016745)).icon(markerIcon).title("Security Office"));//Head Office
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.176844, 80.021178)).icon(markerIcon).title("Central Mess"));//Mess
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.176057, 80.022696)).icon(markerIcon).title("SAC"));//SAC
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.178452, 80.024609)).icon(markerIcon).title("Hexagon"));//Hexagon
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.176806, 80.024721)).icon(markerIcon).title("Auditorium"));//Audi
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.175971, 80.024081)).icon(markerIcon).title("Power House"));//Power House
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.175690, 80.027027)).icon(markerIcon).title("Near New CC"));//PHC-CC Circle
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.176048, 80.027722)).icon(markerIcon).title("PHC"));//PHC
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.179325, 80.027275)).icon(markerIcon).title("Admin. Office"));//Administrative Office
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.178192, 80.026231)).icon(markerIcon).title("ECE Lab"));//ECE Lab
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.174448, 80.027921)).icon(markerIcon).title("Visitor's Hostel"));//VH
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.171737, 80.033542)).icon(markerIcon).title("NR"));//NR1
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.172652, 80.032960)).icon(markerIcon).title("NR"));//NR2
    }

    private void addingInitFirebaseData(final GoogleMap googleMap){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference("alerts");
        reference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, dataSnapshot.toString());
                String level;
                level = dataSnapshot.child("leopard/level").getValue().toString();
                if (level.equals("1")) {
                    double latitude, longitude;
                    long id;
                    id = (Long) dataSnapshot.child("id").getValue();
                    latitude = (Double) dataSnapshot.child("latitude").getValue();
                    longitude = (Double) dataSnapshot.child("longitude").getValue();
                    markerAdder(id, level, latitude, longitude);

                    //Adding a delay handler so the data on Firebase changes automatically after 10 minutes
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Log.i("tag", "Updating Firebase Database in 5 minutes");
                                    dataSnapshot.getRef().child("leopard").child("level").setValue("0");
                                }
                            },
                            300000
                    );
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static final String CHANNEL_ID="ALERT";
    private static final String CHANNEL_NAME="STAYSAFE";
    private static final String CHANNEL_DESC="LEOPARD DETECTED";

    Button sos_button;
    Button contact_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.map_activity);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager =getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(channel);
            lm = (LocationManager)this.getSystemService(MapActivity.this.LOCATION_SERVICE);
        }

        getLocationPermission();
        markerHashMap = new HashMap<>();

        sos_button = findViewById(R.id.button1);

        sos_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                @SuppressLint("SimpleDateFormat") SimpleDateFormat stf = new SimpleDateFormat("k" );
                String currentTime = stf.format(new Date());
                Log.d("date", currentTime);
                int time_check = Integer.parseInt(currentTime);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd");
                String currentDate = sdf.format(new Date());
                Log.d("date", currentDate);
                int date_check = Integer.parseInt(currentDate);

                String phone;

                //Call via the SOS button on the Supervisor based on Date and Time provided in Contacts
                //Requires permission that needs to be given in settings
                //Add phone numbers to 'String phone' that are needed to be called
                if (date_check <= 10) {
                    if (time_check >= 6 && time_check <= 12) {
                        phone = "+91 11111 11111";
                    }
                    else if (time_check > 12 && time_check <= 22) {
                        phone = "+91 22222 22222";
                    }
                    else {
                        phone = "+91 33333 33333";
                    }
                }
                else if (date_check <= 20){
                    if (time_check >= 6 && time_check <= 12) {
                        phone = "+91 33333 33333";
                    }
                    else if (time_check > 12 && time_check <= 22) {
                        phone = "+91 11111 11111";
                    }
                    else {
                        phone = "+91 22222 22222";
                    }
                }
                else {
                    if (time_check >= 6 && time_check <= 12) {
                        phone = "+91 22222 22222";
                    }
                    else if (time_check > 12 && time_check <= 22) {
                        phone = "+91 33333 33333";
                    }
                    else {
                        phone = "+91 11111 11111";
                    }
                }

                String s = "tel: " + phone;
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(s));
                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                    if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        startActivity(intent);
                    }
                }
                else {
                    startActivity(intent);
                }
            }
        });

        //Open contacts list according to date
        contact_Button = findViewById(R.id.button2);
        contact_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }

            void openActivity2() {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd");
                String currentDateAndTime = sdf.format(new Date());
                Log.d("date", currentDateAndTime);
                int date_check = Integer.parseInt(currentDateAndTime);

                if (date_check <= 10) {
                    Intent intent1 = new Intent(MapActivity.this, activity_contacts_1.class);
                    startActivity(intent1);
                }
                else if (date_check <= 20) {
                    Intent intent2 = new Intent(MapActivity.this, activity_contacts_2.class);
                    startActivity(intent2);
                } else {
                    Intent intent3 = new Intent(MapActivity.this, activity_contacts_3.class);
                    startActivity(intent3);
                }
            }
        });
    }

    private void initMap(){
        Log.d(TAG, "initMap: Initializing the map");
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(MapActivity.this);
        cardView = findViewById(R.id.card_view);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Getting location permissions");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        int LOCATION_PERMISSION_REQUEST_CODE = 1234;
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            }
            else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;
        if (requestCode == 1234) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = false;
                        Log.d(TAG, "onRequestPermissionsResult: permission failed");
                        return;
                    }
                }
                mLocationPermissionGranted = true;
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
                initMap();
            }
        }
    }

    public void addingFirebaseData(final GoogleMap googleMap){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference("alerts");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, dataSnapshot.toString());
                double latitude, longitude;
                long id;
                String level, area;
                id = (Long)dataSnapshot.child("id").getValue();
                latitude = (Double)dataSnapshot.child("latitude").getValue();
                longitude = (Double)dataSnapshot.child("longitude").getValue();
                level = dataSnapshot.child("leopard/level").getValue().toString();
                area = dataSnapshot.child("area").getValue().toString();
                Log.d(TAG, "onChildAdded:" + "id: " + id + latitude + longitude + level + area);
                markerAdder(id, level, latitude, longitude);
                if (level.equals("1")) {
                    displayNotification(area, latitude, longitude);

                    //Adding a delay handler so the data on Firebase changes automatically after 10 minutes
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Log.i("tag", "Updating Firebase Database in 5 minutes");
                                    dataSnapshot.getRef().child("leopard").child("level").setValue("0");
                                }
                            },
                            10000
                    );

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: " + marker.getTitle());

        final TextView judgement, area, id;
        judgement = findViewById(R.id.judgement_card);
        area = findViewById(R.id.area_card);
        id = findViewById(R.id.id_card);

        final String id_cs = marker.getTitle();
        if (id_cs.contains("Camera ID: ")) {
            final String id_s = id_cs.substring(11);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference("alerts");
            reference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    if (id_s.equals(dataSnapshot.child("id").getValue().toString())) {
                        String p = dataSnapshot.child("leopard").child("level").getValue().toString();

                        if (p.equals("1")) {
                            cardView.setVisibility(View.VISIBLE);
                            judgement.setText("Leopard Detected");
                        }

                        area.setText(dataSnapshot.child("area").getValue().toString());
                        id.setText(dataSnapshot.child("id").getValue().toString());
                        Log.d(TAG, "onChildAdded: " + dataSnapshot.getValue().toString());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return false;
    }
}
