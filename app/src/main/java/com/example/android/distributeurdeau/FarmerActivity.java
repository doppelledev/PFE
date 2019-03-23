package com.example.android.distributeurdeau;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.distributeurdeau.models.Farmer;

import jade.core.MicroRuntime;
import jade.core.NotFoundException;
import jade.wrapper.ControllerException;

public class FarmerActivity extends AppCompatActivity {

    private static final String TAG = "FarmerActivity";

    private Farmer farmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer);

        farmer = (Farmer) getIntent().getSerializableExtra("farmer");
        Log.d(TAG, "onCreate: farmer: " + farmer.getFarmer_num());
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
