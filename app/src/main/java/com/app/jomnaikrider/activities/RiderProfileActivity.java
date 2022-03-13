package com.app.jomnaikrider.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.app.jomnaikrider.R;
import com.app.jomnaikrider.models.RiderModelCLass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//User Profile Screen..
public class RiderProfileActivity extends BaseActivity {

    ImageView imgEdit;
    EditText edtName, edtPhone,edtAddress,  edtEmail, editPassword;
    Spinner spnGender;
    Button btnUpdate, btnUpdatePass;
    TextView tvEmailStatus;
    String userId, userName,address, userPhone, password, gender, email;
    DatabaseReference databaseReference;
    boolean emailVerified;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_profile);

        //Get user data from user screen..

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userName = RiderActivity.fullName;
        userPhone = RiderActivity.phone;
        password = RiderActivity.password;
        address = RiderActivity.address;
        gender = RiderActivity.gender;
        email = RiderActivity.email;
        password = RiderActivity.password;

        //Firebase and screen views initialization..

        databaseReference = FirebaseDatabase.getInstance().getReference("RidersData");

        imgEdit = findViewById(R.id.imgEdit);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtEmail = findViewById(R.id.edtEmail);
        editPassword = findViewById(R.id.editPassword);
        spnGender = findViewById(R.id.spnGender);
        tvEmailStatus = findViewById(R.id.tvEmailStatus);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdatePass = findViewById(R.id.btnUpdatePass);

        edtName.setText(userName);
        edtPhone.setText(userPhone);
        edtAddress.setText(address);
        if(gender.equals("Male")){
            spnGender.setSelection(1);
        }else if(gender.equals("Female")){
            spnGender.setSelection(2);
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        edtEmail.setText(firebaseUser.getEmail());
        editPassword.setText(password);

        edtName.setEnabled(false);
        edtPhone.setEnabled(false);
        edtAddress.setEnabled(false);
        edtEmail.setEnabled(false);
        editPassword.setEnabled(false);

        tvEmailStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkEmailIsVerified()){
                    sendVerificationEmail();
                }
            }
        });

        //Edit sign clicks code
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                if(count%2 != 0) {
                    edtName.setEnabled(true);
                    edtName.requestFocus();
                    edtPhone.setEnabled(true);
                    edtAddress.setEnabled(true);
                }else {
                    edtName.setEnabled(false);
                    edtPhone.setEnabled(false);
                    edtAddress.setEnabled(false);
                }
            }
        });
        //Update button click code
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = edtName.getText().toString().trim();
                userPhone = edtPhone.getText().toString().trim();
                address = edtAddress.getText().toString().trim();
                gender = spnGender.getSelectedItem().toString();

                //Validations to all data fields..
                if(TextUtils.isEmpty(userName)){
                    edtName.setError("Required!");
                    edtName.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(userPhone)){
                    edtPhone.setError("Required!");
                    edtPhone.requestFocus();
                    return;
                }
                if(userPhone.length()>10 || userPhone.length()<10){
                    edtPhone.setError("Enter valid phone number!!");
                    edtPhone.requestFocus();
                }
                if(TextUtils.isEmpty(address)){
                    edtAddress.setError("Required!");
                    edtAddress.requestFocus();
                    return;
                }
                if(gender.equals("Select Gender")){
                    Toast.makeText(getApplicationContext(), "Please select gender!", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateAccount();
            }
        });

        //Send update password email code..
        btnUpdatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Password reset email sent successfully", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(), "Error : "+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //Update account details function..
    private void updateAccount() {
        RiderModelCLass model = new RiderModelCLass(userId,userName,email,userPhone,address,
                password,RiderActivity.token,RiderActivity.userType,gender,emailVerified);
        databaseReference.child(userId).setValue(model);
        edtName.setEnabled(false);
        edtPhone.setEnabled(false);
        edtAddress.setEnabled(false);
        editPassword.setEnabled(false);

        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!checkEmailIsVerified()){
            tvEmailStatus.setText("Email not Verified, CLick to verify");
            emailVerified = false;
        }else {
            tvEmailStatus.setText("Email Verified");
            emailVerified = true;
        }
    }

    //Verification method...
    private boolean checkEmailIsVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user.isEmailVerified()){
            return true;
        }else {
            return false;
        }
    }

    private void sendVerificationEmail() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Verification email sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
