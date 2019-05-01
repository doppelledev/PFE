package com.example.android.distributeurdeau.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.MainActivity;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.RegisterActivity;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.farmer.FarmerActivity;
import com.example.android.distributeurdeau.farmer.FarmerAgent;
import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Supervisor;
import com.example.android.distributeurdeau.supervisor.SupervisorActivity;
import com.example.android.distributeurdeau.supervisor.SupervisorAgent;

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

    private boolean isFarmer = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.login));

        setupViews();

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);

        try {
            loginInterface = MicroRuntime.getAgent(MainActivity.initial_agent_name)
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
        filter.addAction(Strings.ACTION_LAUNCH_SUPERVISOR);
        registerReceiver(receiver, filter);


    }

    private void setupViews() {
        Button loginB = findViewById(R.id.loginB);
        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 login();
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

    private void login() {
        loginInterface.authenticate(
                loginET.getText().toString(),
                passET.getText().toString(),
                isFarmer
        );
        loginPB.setVisibility(View.VISIBLE);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.farmerRadio:
                if (checked)
                    loginET.setHint(R.string.farmer_num);
                    isFarmer = true;
                    break;
            case R.id.supervisorRadio:
                if (checked)
                    loginET.setHint(R.string.supervisor_num);
                    isFarmer = false;
                    break;
        }
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

    @Override
    protected void onDestroy() {
        // kill the initial agent and dispose of the service and the receiver
        Log.d(TAG, "onDestroy: destroying");
        try {
            MicroRuntime.killAgent(MainActivity.initial_agent_name);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        unbindService(MainActivity.serviceConnection);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    // This class takes care of deploying the FarmerAgent in a background thread
    private static class DeployUser extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... args) {
            boolean isFarmer = (boolean) args[2];
            if (isFarmer) {
                try {
                    MicroRuntime.startAgent(
                            Strings.FARMER_PREFIX + ((Farmer) args[1]).getFarmer_num(),
                            FarmerAgent.class.getName(),
                            args
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    MicroRuntime.startAgent(
                            Strings.SUPERVISOR_PREFIX + ((Supervisor) args[1]).getId(),
                            SupervisorAgent.class.getName(),
                            args
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            final String action = intent.getAction();
            if (action == null)
                return;

            switch (action) {
                case Strings.ACTION_LOGIN_SUCCEEDED: {
                    // if login succeeded, deploy the farmer agent
                    Log.d(TAG, "onReceive: login succeeded");
                    if (isFarmer) {
                        Farmer farmer = (Farmer) intent.getSerializableExtra(Strings.EXTRA_FARMER);
                        new DeployUser().execute(getApplicationContext(), farmer, isFarmer);
                    } else {
                        Supervisor supervisor = (Supervisor) intent.getSerializableExtra(Strings.EXTRA_SUPERVISOR);
                        new DeployUser().execute(getApplicationContext(), supervisor, isFarmer);
                    }
                    break;
                }
                case Strings.ACTION_LOGIN_FAILED:
                    failed();
                    break;
                case Strings.ACTION_LAUNCH_FARMER: {
                    // launch the farmer activity
                    // This is triggered by the deployed farmer agent
                    Farmer farmer = (Farmer) intent.getSerializableExtra(Strings.EXTRA_FARMER);
                    Intent farmerIntent = new Intent(LoginActivity.this, FarmerActivity.class);
                    farmerIntent.putExtra(Strings.EXTRA_FARMER, farmer);
                    farmerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(farmerIntent);
                    break;
                }
                case Strings.ACTION_LAUNCH_SUPERVISOR: {
                    // launch the supervisor activity
                    // This is triggered by the deployed supervisor agent
                    Supervisor supervisor = (Supervisor) intent.getSerializableExtra(Strings.EXTRA_SUPERVISOR);
                    Intent supervisorIntent = new Intent(LoginActivity.this, SupervisorActivity.class);
                    supervisorIntent.putExtra(Strings.EXTRA_SUPERVISOR, supervisor);
                    supervisorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(supervisorIntent);
                    break;
                }
            }
        }
    }
}
