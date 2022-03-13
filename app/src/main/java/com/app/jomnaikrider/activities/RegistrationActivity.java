package com.app.jomnaikrider.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.jomnaikrider.R;
import com.app.jomnaikrider.models.RiderModelCLass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//Sign up form screen fro getting name, address etc from user
public class RegistrationActivity extends BaseActivity {

    EditText edtName, edtPhone, edtMotorcycle;
    Button btnRegister;
    Spinner spnGender;
    String fullName, phone, address,gender, token;
    FirebaseAuth mAuth;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //Firebase database and authentication initialization...

        mAuth = FirebaseAuth.getInstance();

        //Get device token for sending notifications using fcm..
        SharedPreferences e = getSharedPreferences("token",MODE_PRIVATE);
        token = e.getString("id","null");

        //All the Views of screen and firebase initialization..
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtMotorcycle = findViewById(R.id.edtMotorcycle);
        spnGender = findViewById(R.id.spnGender);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullName = edtName.getText().toString().trim();
                phone = edtPhone.getText().toString().trim();
                address = edtMotorcycle.getText().toString().trim();
                gender = spnGender.getSelectedItem().toString();

                if(TextUtils.isEmpty(fullName)){
                   edtName.setError("Required!");
                   edtName.requestFocus();
                   return;
                }
                if(TextUtils.isEmpty(phone)){
                    edtPhone.setError("Required!");
                    edtPhone.requestFocus();
                    return;
                }
                if(phone.length()>10 || phone.length()<10){
                    edtPhone.setError("Enter valid phone number!!");
                    edtPhone.requestFocus();
                }
                if(TextUtils.isEmpty(address)){
                    edtMotorcycle.setError("Required!");
                    edtMotorcycle.requestFocus();
                    return;
                }
                if(gender.equals("Select Gender")){
                    Toast.makeText(getApplicationContext(), "Please select gender!", Toast.LENGTH_SHORT).show();
                    return;
                }
                createAccount();
            }
        });
    }

    //This method store all the data fileds of user to firebase...
    private void createAccount(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("RidersData");
        RiderModelCLass model = new RiderModelCLass(userId,fullName,AuthenticationActivity.email,phone,address,
                AuthenticationActivity.password,token,"Rider",gender,VerificationActivity.emailVerified);
        databaseReference.child(userId).setValue(model);
        Toast.makeText(getApplicationContext(), "Your details saved", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), RiderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1500, 50, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1500, 50, locationListener);
                }
            }
        }
    }
}
