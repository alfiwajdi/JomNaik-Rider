package com.app.jomnaikrider.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.jomnaikrider.R;
import com.app.jomnaikrider.helpers.MyFirebaseInstanceService;
import com.app.jomnaikrider.models.BookingModelClass;
import com.app.jomnaikrider.models.DriverModelCLass;
import com.app.jomnaikrider.models.RatingModelClass;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class BookARideActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesAutoCompleteAdapter.ClickListener {

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static int REQUEST_PHONE_CALL = 2;
    boolean mGranted;
    private GoogleMap mMap;
    EditText edtSearch;
    ImageView imgSearch, imgWeatherDropOff;
    List<Address> addressList;
    MapView mapView;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    private RecyclerView recyclerView;
    LocationManager locationManager;
    LocationListener locationListener;
    String userId, bookingId, oldDriverId;
    Context context = BookARideActivity.this;
    Location location1, location2;
    double distance;
    Button btnFindDriver, btnCallDriver;
    CardView card;
    TextView tvFinding, tvCancelRide, tvHi, tvWhereTo, tvDriverComing, tvBikeName, tvDriverName;
    View v1, v2;
    ImageView imgCancel;
    List<DriverModelCLass> driversList;
    List<BookingModelClass> bookingsList;
    boolean flag = false, flag2 = false;
    AlertDialog alertDialog;
    DriverModelCLass driver;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_a_ride);

        Places.initialize(this, getResources().getString(R.string.google_maps_key));
        userId  = FirebaseAuth.getInstance().getCurrentUser().getUid();

        driversList = new ArrayList<>();
        bookingsList = new ArrayList<>();

        mapView = findViewById(R.id.mapView);
        edtSearch = findViewById(R.id.edtSearch);
        imgSearch = findViewById(R.id.imgSearch);
        imgWeatherDropOff = findViewById(R.id.imgWeatherDropOff);
        btnFindDriver = findViewById(R.id.btnFindDriver);
        btnCallDriver = findViewById(R.id.btnCallDriver);
        card = findViewById(R.id.card);
        tvHi = findViewById(R.id.tvHi);
        tvWhereTo = findViewById(R.id.tvWhereTo);
        tvFinding = findViewById(R.id.tvFinding);
        tvCancelRide = findViewById(R.id.tvCancelRide);
        imgCancel = findViewById(R.id.imgCancel);
        tvDriverComing = findViewById(R.id.tvDriverComing);
        v1 = findViewById(R.id.v1);
        tvBikeName = findViewById(R.id.tvBikeName);
        tvDriverName = findViewById(R.id.tvDriverName);
        v2 = findViewById(R.id.v2);
        recyclerView = findViewById(R.id.places_recycler_view);

//        edtSearch.addTextChangedListener(filterTextWatcher);

        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAutoCompleteAdapter.setClickListener(this);
        recyclerView.setAdapter(mAutoCompleteAdapter);

        mAutoCompleteAdapter.notifyDataSetChanged();
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        location1 = new Location("Location1");
        location2 = new Location("Location2");

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = edtSearch.getText().toString().trim();
                addressList = null;
                if(location.isEmpty()){
                    recyclerView.setVisibility(View.GONE);
                }
                if(location != null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(context);
                    try {
                        addressList = geocoder.getFromLocationName(location,2);
                        if(addressList != null){
                            recyclerView.setVisibility(View.VISIBLE);
                            PlacesListAdapter adapter = new PlacesListAdapter(context, addressList);
                            recyclerView.setAdapter(adapter);
                        }else {
                            Toast.makeText(context, "No location found", Toast.LENGTH_SHORT).show();
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        imgWeatherDropOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                intent.putExtra("lati",location1.getLatitude());
                intent.putExtra("longi",location1.getLongitude());
                startActivity(intent);
            }
        });
        btnFindDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("DriversData");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            DriverModelCLass model = snapshot1.getValue(DriverModelCLass.class);
                            flag = false;
                            if(RiderActivity.gender.equals(model.getGender()) && model.getAvailable()==true){
                                for(BookingModelClass booking : bookingsList){
                                    if(booking.getDriverId().equals(model.getUserId()) && booking.getStatus().equals("Pending")){
                                        flag = true;
                                        break;
                                    }
                                }
                                if(flag){
                                    continue;
                                }else {
                                    driversList.add(model);
                                }
                            }
                        }
                        if(driversList.size()>0){
                            int size = driversList.size();
                            int random = new Random().nextInt(size-0)+0;
                            Toast.makeText(getApplicationContext(), "random : "+random, Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), "data : "+driversList.get(random).getFullName(), Toast.LENGTH_SHORT).show();


                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("BookingRequests");
                            bookingId = dbRef.push().getKey();

                            String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            driver = driversList.get(random);

                            BookingModelClass modelClass = new BookingModelClass(bookingId,riderId,RiderActivity.fullName,RiderActivity.phone,RiderActivity.gender,
                                    RiderActivity.token,driver.getUserId(),driver.getFullName(),driver.getPhone(),location2.getLatitude(),location2.getLongitude(),
                                    location1.getLatitude(),location1.getLongitude(),"Pending");
                            dbRef.child(bookingId).setValue(modelClass);

                            tvFinding.setVisibility(View.VISIBLE);
                            tvCancelRide.setVisibility(View.VISIBLE);
                            imgCancel.setVisibility(View.VISIBLE);

                            tvHi.setVisibility(View.GONE);
                            tvWhereTo.setVisibility(View.GONE);
                            btnFindDriver.setVisibility(View.GONE);
                            card.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);

                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> listAddresses = geocoder.getFromLocation(location1.getLatitude(), location1.getLongitude(), 1);

                                if (listAddresses != null && listAddresses.size() > 0) {
                                    String address = "";

                                    if (listAddresses.get(0).getAddressLine(0) != null) {
                                        address += listAddresses.get(0).getAddressLine(0);
                                        new MyFirebaseInstanceService().sendMessageSingle(BookARideActivity.this, driver.getToken(), "New Ride Request", "Drop-Off Address, "+address, null);
                                    }else {
                                        if (listAddresses.get(0).getThoroughfare() != null) {
                                            address += listAddresses.get(0).getThoroughfare() + ", ";
                                        }
                                        if (listAddresses.get(0).getLocality() != null) {
                                            address += listAddresses.get(0).getLocality() + ", ";
                                        }
                                        if (listAddresses.get(0).getAdminArea() != null) {
                                            address += listAddresses.get(0).getAdminArea()+", ";
                                        }
                                        if (listAddresses.get(0).getCountryName() != null) {
                                            address += listAddresses.get(0).getCountryName();
                                        }
                                        new MyFirebaseInstanceService().sendMessageSingle(BookARideActivity.this, driver.getToken(), "New Ride Request", "Drop-Off Address, "+address, null);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "No volunteer drivers registered in app yet!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvFinding.setVisibility(View.GONE);
                tvCancelRide.setVisibility(View.GONE);
                imgCancel.setVisibility(View.GONE);

                tvHi.setVisibility(View.VISIBLE);
                tvWhereTo.setVisibility(View.VISIBLE);
                card.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                edtSearch.setText("");
                recyclerView.setAdapter(null);

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("BookingRequests").child(bookingId);
                dbRef.child("status").setValue("Cancelled");
                Toast.makeText(getApplicationContext(), "Trip Cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnCallDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Call to Rider", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!mGranted) {
                        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                            return;
                        }
                    }
                }
                String phone = driver.getPhone();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel: +6"+phone));
                startActivity(intent);
            }
        });
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals("")) {
                mAutoCompleteAdapter.getFilter().filter(s.toString());
                if (recyclerView.getVisibility() == View.GONE) {recyclerView.setVisibility(View.VISIBLE);}
            } else {
                if (recyclerView.getVisibility() == View.VISIBLE) {recyclerView.setVisibility(View.GONE);}
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
    };

    @Override
    public void click(Place place) {
        Toast.makeText(this, place.getAddress()+", "+place.getLatLng().latitude+place.getLatLng().longitude, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                //mMap.clear();

                location2.setLatitude(location.getLatitude());
                location2.setLongitude(location.getLongitude());

                imgSearch.setVisibility(View.VISIBLE);
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }
            @Override
            public void onProviderEnabled(String s) { }
            @Override
            public void onProviderDisabled(String s) { }
        };
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

        showProgressDialog("Loading map..");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("BookingRequests");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    BookingModelClass model = snapshot1.getValue(BookingModelClass.class);
                    bookingsList.add(model);

                    if(model.getId().equals(bookingId) && model.getStatus().equals("Reject")){
                        oldDriverId = model.getDriverId();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("BookingRequests").child(model.getId());
                        databaseReference.removeValue();

                        if(driversList.size()>0){
                            int size = driversList.size();
                            int random = new Random().nextInt(size-0)+0;
                            Toast.makeText(getApplicationContext(), "random : "+random, Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), "data : "+driversList.get(random).getFullName(), Toast.LENGTH_SHORT).show();


                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("BookingRequests");
                            bookingId = dbRef.push().getKey();

                            String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DriverModelCLass driver = driversList.get(random);

                            BookingModelClass modelClass = new BookingModelClass(bookingId,riderId,RiderActivity.fullName,RiderActivity.phone,RiderActivity.gender,
                                    RiderActivity.token,driver.getUserId(),driver.getFullName(),driver.getPhone(),location2.getLatitude(),location2.getLongitude(),
                                    location1.getLatitude(),location1.getLongitude(),"Pending");
                            dbRef.child(bookingId).setValue(modelClass);

                            tvFinding.setVisibility(View.VISIBLE);
                            tvCancelRide.setVisibility(View.VISIBLE);
                            imgCancel.setVisibility(View.VISIBLE);

                            tvHi.setVisibility(View.GONE);
                            tvWhereTo.setVisibility(View.GONE);
                            btnFindDriver.setVisibility(View.GONE);
                            card.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);

                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> listAddresses = geocoder.getFromLocation(location1.getLatitude(), location1.getLongitude(), 1);

                                if (listAddresses != null && listAddresses.size() > 0) {
                                    String address = "";

                                    if (listAddresses.get(0).getAddressLine(0) != null) {
                                        address += listAddresses.get(0).getAddressLine(0);
                                        new MyFirebaseInstanceService().sendMessageSingle(BookARideActivity.this, driver.getToken(), "New Ride Request", "Drop-Off Address, "+address, null);
                                    }else {
                                        if (listAddresses.get(0).getThoroughfare() != null) {
                                            address += listAddresses.get(0).getThoroughfare() + ", ";
                                        }
                                        if (listAddresses.get(0).getLocality() != null) {
                                            address += listAddresses.get(0).getLocality() + ", ";
                                        }
                                        if (listAddresses.get(0).getAdminArea() != null) {
                                            address += listAddresses.get(0).getAdminArea()+", ";
                                        }
                                        if (listAddresses.get(0).getCountryName() != null) {
                                            address += listAddresses.get(0).getCountryName();
                                        }
                                        new MyFirebaseInstanceService().sendMessageSingle(BookARideActivity.this, driver.getToken(), "New Ride Request", "Drop-Off Address, "+address, null);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "No volunteer drivers is available now!", Toast.LENGTH_LONG).show();
                        }
                    }else if(model.getId().equals(bookingId) && model.getStatus().equals("Accept")){
                        flag2 = true;

                        tvFinding.setVisibility(View.GONE);
                        tvCancelRide.setVisibility(View.GONE);
                        imgCancel.setVisibility(View.GONE);

                        tvDriverComing.setVisibility(View.VISIBLE);
                        tvDriverComing.setText("Driver is Coming");
                        v1.setVisibility(View.VISIBLE);
                        tvBikeName.setVisibility(View.VISIBLE);
                        tvBikeName.setText(model.getRiderToken());
                        tvDriverName.setVisibility(View.VISIBLE);
                        tvDriverName.setText(model.getDriverName());
                        v2.setVisibility(View.VISIBLE);
                        card.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        btnFindDriver.setVisibility(View.GONE);
                        btnCallDriver.setVisibility(View.VISIBLE);
                    }else if(model.getId().equals(bookingId) && model.getStatus().equals("Cancelled")){
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("BookingRequests").child(model.getId());
                        databaseReference.removeValue();
                        Toast.makeText(getApplicationContext(), "Trip is cancelled by driver!", Toast.LENGTH_LONG).show();
                        finish();
                    }else if(model.getId().equals(bookingId) && model.getStatus().equals("Arrived")){
                        flag2 = true;

                        tvFinding.setVisibility(View.GONE);
                        tvCancelRide.setVisibility(View.GONE);
                        imgCancel.setVisibility(View.GONE);

                        tvDriverComing.setVisibility(View.VISIBLE);
                        tvDriverComing.setText("Driver has Arrived");
                        v1.setVisibility(View.VISIBLE);
                        tvBikeName.setVisibility(View.VISIBLE);
                        tvBikeName.setText(model.getRiderToken());
                        tvDriverName.setVisibility(View.VISIBLE);
                        tvDriverName.setText(model.getDriverName());
                        v2.setVisibility(View.VISIBLE);
                        card.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        btnFindDriver.setVisibility(View.GONE);
                        btnCallDriver.setVisibility(View.VISIBLE);
                    }else if(model.getId().equals(bookingId) && model.getStatus().equals("Started")){
                        flag2 = true;
                        tvFinding.setVisibility(View.GONE);
                        tvCancelRide.setVisibility(View.GONE);
                        imgCancel.setVisibility(View.GONE);

                        tvDriverComing.setVisibility(View.VISIBLE);
                        tvDriverComing.setText("Going to Destination");
                        v1.setVisibility(View.VISIBLE);
                        tvBikeName.setVisibility(View.VISIBLE);
                        tvBikeName.setText(model.getRiderToken());
                        tvDriverName.setVisibility(View.VISIBLE);
                        tvDriverName.setText(model.getDriverName());
                        v2.setVisibility(View.VISIBLE);
                        card.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        btnFindDriver.setVisibility(View.GONE);
                        btnCallDriver.setVisibility(View.VISIBLE);
                    }else if(model.getId().equals(bookingId) && model.getStatus().equals("Completed")){
                        showRatingDialog(model);
                    }
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        hideProgressDialog();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.ImageViewHolder>{
        private Context mcontext ;
        private List<Address> muploadList;

        public PlacesListAdapter(Context context , List<Address> uploadList ) {
            mcontext = context ;
            muploadList = uploadList ;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.place_recycler_item_layout, parent , false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final Address address = muploadList.get(position);

            holder.place_address.setText(address.getLocality()+", "+address.getAdminArea()+", "+address.getCountryName());
            holder.place_area.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.clear();
                    Address address = addressList.get(position);
                    location1.setLatitude(address.getLatitude());
                    location1.setLongitude(address.getLongitude());
                    edtSearch.setText(address.getLocality()+", "+address.getAdminArea()+", "+address.getCountryName());
                    Toast.makeText(context, "Drop-Off location selected", Toast.LENGTH_SHORT).show();

                    distance = location1.distanceTo(location2)/1000;
                    mapView.setVisibility(View.VISIBLE);

                    LatLng userLocation = new LatLng(location1.getLatitude(), location1.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Drop-Off").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10));

                    LatLng userLocation2 = new LatLng(location2.getLatitude(), location2.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation2).title("My Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation2,10));

                    mMap.addPolyline(
                            new PolylineOptions()
                                    .add(userLocation)
                                    .add(userLocation2)
                                    .width(5f)
                                    .color(Color.RED)
                    );
                    recyclerView.setVisibility(View.GONE);
                    btnFindDriver.setVisibility(View.VISIBLE);
                    imgWeatherDropOff.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder{
            public TextView place_address;
            public TextView place_area;

            public ImageViewHolder(View itemView) {
                super(itemView);

                place_address = itemView.findViewById(R.id.place_address);
                place_area = itemView.findViewById(R.id.place_area);

            }
        }
    }

    private void showRatingDialog(BookingModelClass model) {
        try {
            AlertDialog.Builder dailogBuilder = new AlertDialog.Builder(BookARideActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_layout, null);
            dailogBuilder.setView(dialogView);

            final RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
            final Button btnRate = dialogView.findViewById(R.id.btnRate);
            final TextView tvRating = dialogView.findViewById(R.id.tvRating);

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if(rating==0.5 || rating==1){
                        tvRating.setText("Very Bad");
                    }else if(rating==1.5 || rating==2){
                        tvRating.setText("Bad");
                    }else if(rating==2.5 || rating==3){
                        tvRating.setText("Good");
                    }else if(rating==3.5 || rating==4){
                        tvRating.setText("Very Good");
                    }else if(rating==4.5 || rating==5){
                        tvRating.setText("Excellent");
                    }else if(rating==0){
                        tvRating.setText("");
                    }
                }
            });
            btnRate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    float rate = ratingBar.getRating();
                    if(rate>0){
                        alertDialog.dismiss();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("DriverRatings").child(model.getDriverId());
                        String id = databaseReference.push().getKey();
                        RatingModelClass modelClass = new RatingModelClass(id,rate,userId,RiderActivity.fullName,model.getDriverId(),model.getId());
                        databaseReference.child(id).setValue(modelClass);
                        Toast.makeText(getApplicationContext(), "Rating added", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
            alertDialog = dailogBuilder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
        catch (WindowManager.BadTokenException e) {
            //use a log message
            e.printStackTrace();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    private ProgressDialog mProgressDialog;

    //This function show progress dialog on screen with user custom message.
    public void showProgressDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    //This function hide progress dialog from screen.
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if(flag2){
            AlertDialog.Builder builder = new AlertDialog.Builder(BookARideActivity.this);
            builder.setTitle("Confirmation?");
            builder.setMessage("Are you sure to cancel the ride?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("BookingRequests").child(bookingId);
                    dbRef.child("status").setValue("Cancelled");
                    finish();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else {
            finish();
        }
    }
}