package com.example.android.distributeurdeau;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.distributeurdeau.models.Farmer;

import jade.android.MicroRuntimeService;
import jade.core.MicroRuntime;
import jade.core.NotFoundException;
import jade.wrapper.ControllerException;

public class FarmerActivity extends AppCompatActivity {

    private static final String TAG = "FarmerActivity";

    private Farmer farmer;
    private LoginInterface loginInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer);

        farmer = (Farmer) getIntent().getSerializableExtra("farmer");
        Log.d(TAG, "onCreate: farmer: " + farmer.getFarmer_num());


        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);
        try {
            loginInterface =
                    MicroRuntime
                            .getAgent(farmer.getFarmer_num())
                            .getO2AInterface(LoginInterface.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: error: " + e);
        }
        TextView b = findViewById(R.id.farmerB);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginInterface != null) {
                    loginInterface.authenticate("hhhh", "hhhhhhh");
                } else {
                    Log.d(TAG, "onCreate: iinterface is null");
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.farmer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                logout();
                break;
        }
        return true;
    }

    private void logout() {
        try {
            Log.d(TAG, "logout: killing agent");
            MicroRuntime.killAgent(farmer.getFarmer_num());
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (NotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "logout: error: " + e);
        }
    }


    @Override
    public void onBackPressed() {
        logout();
    }
}
