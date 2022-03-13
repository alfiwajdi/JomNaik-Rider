package com.app.jomnaikrider.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.jomnaikrider.R;
import com.app.jomnaikrider.models.RiderModelCLass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//Driver Home Page Screen Activity File.
public class RiderActivity extends BaseActivity {

    Button btnBookARide, btnViewHistory,btnAboutApp, btnProfile, btnSignOut;
    TextView tvName;
    String userId="";
    DatabaseReference databaseReference;
    public static String fullName, phone, address, token, userType, gender, email, password;
    public static boolean emailVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);


        //Firebase realtime database initialization for driver.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("RidersData").child(userId);


        //Views initialization of driver screen.
        tvName = findViewById(R.id.tvName);
        btnBookARide = findViewById(R.id.btnBookARide);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnAboutApp = findViewById(R.id.btnAboutApp);
        btnProfile = findViewById(R.id.btnProfile);
        btnSignOut = findViewById(R.id.btnSignOut);

        if(!checkEmailIsVerified()){
            emailVerified = false;
            tvName.setText("Email not verified");
            AlertDialog.Builder builder = new AlertDialog.Builder(RiderActivity.this);
            builder.setTitle("Alert!");
            builder.setMessage("Please verify your email from profile screen for using app properly!").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else {
            emailVerified = true;
        }

        //Find Nearby Mechanics button code
        btnBookARide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailVerified){
                    Intent intent = new Intent(getApplicationContext(), BookARideActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(getApplicationContext(), "Please verify your email first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //View Mechanics  response button code
        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailVerified){
                    Intent intent = new Intent(getApplicationContext(), ViewHistoryActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(getApplicationContext(), "Please verify your email first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnAboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AboutAppActivity.class);
                startActivity(intent);
            }
        });

        //View Profile button code
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RiderProfileActivity.class));
            }
        });

        //Sign Out button code
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RiderActivity.this);
                builder.setTitle("Confirmation?");
                builder.setMessage("Are you sure to sign out?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseReference.child("token").setValue("null");
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), RiderLoginActivity.class));
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
            }
        });

        //Getting location permissions code from user
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {

            }
        }
        checkLocationEnabled();
    }

    private void checkLocationEnabled() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(RiderActivity.this)
                    .setMessage("Location/Gps not enabled, Please enable to use app functions properly")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    //Getting current logged in driver data from firebase realtime database.
    private void loadUserData() {

        showProgressDialog("Preparing app functions..");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RiderModelCLass model = snapshot.getValue(RiderModelCLass.class);
                fullName = model.getFullName();
                address = model.getAddress();
                phone = model.getPhone();
                password = model.getPassword();
                email = model.getEmail();
                gender = model.getGender();
                userType = model.getUserType();
                token = model.getToken();

                for(DataSnapshot snapshot1 : snapshot.getChildren()){ }

                tvName.setText("Rider : "+fullName);
                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { hideProgressDialog();}});
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadUserData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

    private boolean checkEmailIsVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user.isEmailVerified()){
            return true;
        }else {
            return false;
        }
    }

}