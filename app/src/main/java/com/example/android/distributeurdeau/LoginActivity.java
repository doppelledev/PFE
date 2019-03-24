package com.example.android.distributeurdeau;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.models.Farmer;

import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.NotFoundException;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    public static final String START_FARMER_ACTIVITY = "start-farmer-activity";
    public static final String LOGIN_FAILED = "login failed";
    public static final String LOGIN_SUCCEEDED = "login succeeded";

    private MicroRuntimeServiceBinder microRuntimeServiceBinder;
    private ServiceConnection serviceConnection;

    private LoginInterface loginInterface;
    private Receiver receiver;
    private ProgressBar loginPB;
    private EditText loginET;
    private EditText passET;
    private TextView registerTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            loginInterface = MicroRuntime.getAgent("Initial Agent")
                    .getO2AInterface(LoginInterface.class);
            Log.d(TAG, "onCreate: interface: " + loginInterface);
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        loginET = findViewById(R.id.loginET);
        passET = findViewById(R.id.passET);

        final Button loginB = findViewById(R.id.loginB);
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

        loginPB = findViewById(R.id.loginPB);
        loginPB.setVisibility(View.GONE);

        registerTV = findViewById(R.id.registerTV);
        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LOGIN_FAILED);
        filter.addAction(LOGIN_SUCCEEDED);
        filter.addAction(START_FARMER_ACTIVITY);
        registerReceiver(receiver, filter);

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);
    }


    private void failed() {
        loginPB.setVisibility(View.GONE);
        Toast.makeText(LoginActivity.this, "Connexion échoué", Toast.LENGTH_SHORT).show();
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            final String action = intent.getAction();
            if (action.equals(LOGIN_SUCCEEDED)) {
                Log.d(TAG, "onReceive: login succeeded");
                Farmer farmer = (Farmer) intent.getSerializableExtra("farmer");
                new DeployFarmer().execute(getApplicationContext(), farmer);
            } else if (action.equals(LOGIN_FAILED)) {
                failed();
            }  else if (action.equals(START_FARMER_ACTIVITY)) {
                Farmer farmer = (Farmer) intent.getSerializableExtra("farmer");
                Intent farmerIntent = new Intent(LoginActivity.this, FarmerActivity.class);
                farmerIntent.putExtra("farmer", farmer);
                farmerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(farmerIntent);
            }
            Log.d(TAG, "onReceive: " + intent.getAction());
        }
    }

    private class DeployFarmer extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... args) {
            try {
                Log.d(TAG, "doInBackground: " + args[1]);
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
        Log.d(TAG, "onDestroy: destroying");
        try {
            MicroRuntime.killAgent(MainActivity.NICK_NAME);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        unbindService(MainActivity.serviceConnection);
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
