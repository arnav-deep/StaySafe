package com.throwntech.staysafe;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.main_activity);
        if(isServicesOK()){
            init();
        }
    }

    public void init(){
        CountDownTimer timer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(mapIntent);
                finish();
            }
        };
        timer.start();
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: Checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOK: Google Play Services is working.");
            return true;
        }

        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServicesOK: An error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "You can not make map requests",Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}