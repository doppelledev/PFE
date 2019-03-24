package com.example.android.distributeurdeau;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.models.Farmer;

import jade.android.MicroRuntimeService;
import jade.core.MicroRuntime;
import jade.core.NotFoundException;
import jade.wrapper.ControllerException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private LoginInterface loginInterface;
    private Receiver receiver;
    private ProgressBar loginPB;
    private EditText loginET;
    private EditText passET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();

        try {
            loginInterface = MicroRuntime.getAgent(MainActivity.INITIAL_AGENT_NAME)
                    .getO2AInterface(LoginInterface.class);
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onCreate: interface: " + loginInterface);

        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_LOGIN_FAILED);
        filter.addAction(Strings.ACTION_LOGIN_SUCCEEDED);
        filter.addAction(Strings.ACTION_LAUNCH_FARMER);
        registerReceiver(receiver, filter);

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void setupViews() {
        Button loginB = findViewById(R.id.loginB);
        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginInterface.authenticate(
                        loginET.getText().toString(),
                        passET.getText().toString()
                );
                loginPB.setVisibility(View.VISIBLE);
            }
        });

        loginET = findViewById(R.id.loginET);
        passET = findViewById(R.id.passET);
        loginPB = findViewById(R.id.loginPB);
        loginPB.setVisibility(View.GONE);
        TextView registerTV = findViewById(R.id.registerTV);
        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }


    private void failed() {
        // handle UI when login fails
        loginPB.setVisibility(View.GONE);
        Toast.makeText(
                LoginActivity.this,
                getString(R.string.toast_login_failed),
                Toast.LENGTH_SHORT
        ).show();
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            final String action = intent.getAction();
            if (action == null)
                return;

            if (action.equals(Strings.ACTION_LOGIN_SUCCEEDED)) {
                // if login succeeded, deploy the farmer agent
                Log.d(TAG, "onReceive: login succeeded");
                Farmer farmer = (Farmer) intent.getSerializableExtra("farmer");
                new DeployFarmer().execute(getApplicationContext(), farmer);
            }
            else if (action.equals(Strings.ACTION_LOGIN_FAILED)) {
                failed();
            }
            else if (action.equals(Strings.ACTION_LAUNCH_FARMER)) {
                // launch the farmer activity
                // This is triggered by the deployed farmer agent
                Farmer farmer = (Farmer) intent.getSerializableExtra(Strings.EXTRA_FARMER);
                Intent farmerIntent = new Intent(LoginActivity.this, FarmerActivity.class);
                farmerIntent.putExtra(Strings.EXTRA_FARMER, farmer);
                farmerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(farmerIntent);
            }
        }
    }

    // This class takes care of deploying the FarmerAgent in a background thread
    private static class DeployFarmer extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... args) {
            try {
                MicroRuntime.startAgent(
                        ((Farmer) args[1]).getFarmer_num(),
                        FarmerAgent.class.getName(),
                        args
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        // kill the initial agent and dispose of the service and the receiver
        Log.d(TAG, "onDestroy: destroying");
        try {
            MicroRuntime.killAgent(MainActivity.INITIAL_AGENT_NAME);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        unbindService(MainActivity.serviceConnection);
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
