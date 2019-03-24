package com.example.android.distributeurdeau;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {
    public static final String LOGIN = "login";

    private static final String TAG = "MainActivity";
    public static final String NICK_NAME = "Initial Agent";
    public static MicroRuntimeServiceBinder microRuntimeServiceBinder;
    public static ServiceConnection serviceConnection;
    private Receiver receiver;

    private Button mainB;
    private TextView mainTV;
    private ProgressBar mainPB;
    public static boolean b = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        microRuntimeServiceBinder = null;

        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LOGIN);
        registerReceiver(receiver, filter);

        mainTV = findViewById(R.id.mainTV);
        mainTV.setVisibility(View.GONE);
        mainPB = findViewById(R.id.mainPB);
        mainPB.setVisibility(View.GONE);
        mainB = findViewById(R.id.mainB);
        mainB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        reset();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reset();
    }


    private void reset() {
        mainB.setText("commencer");
        mainB.setVisibility(View.VISIBLE);
        mainPB.setVisibility(View.GONE);
        try {
            MicroRuntime.killAgent(NICK_NAME);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initiateService();
                mainPB.setVisibility(View.VISIBLE);
                mainTV.setVisibility(View.GONE);
                mainB.setVisibility(View.GONE);
            }
        });
    }

    private void failed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainB.setText("réessayer");
                mainB.setVisibility(View.VISIBLE);
                mainPB.setVisibility(View.GONE);
                mainTV.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Impossible de lancer le service", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initiateService() {
        SharedPreferences sharedPref = getSharedPreferences(
                "networkSettings", Context.MODE_PRIVATE);
        String host = sharedPref.getString("host", "localhost");
        String port = sharedPref.getString("port", "3000");

        final Properties profile = new Properties();
        profile.setProperty(Profile.MAIN_HOST, host);
        profile.setProperty(Profile.MAIN_PORT, port);
        profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
        profile.setProperty(Profile.JVM, Profile.ANDROID);

        if (AndroidHelper.isEmulator()) {
            // Emulator: this is needed to work with emulated devices
            profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
        } else {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            profile.setProperty(Profile.LOCAL_HOST, ip);
            Log.d(TAG, ip);
        }
        //Emulator: this is not really needed on a real device
        profile.setProperty(Profile.LOCAL_PORT, "2000");

        if (microRuntimeServiceBinder == null) {
            serviceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
                    Log.d(TAG, "initiateService(): Gateway successfully bound to MicroRuntimeService");
                    if (b) {
                        startContainer(profile);
                        b = false;
                    }
                }

                public void onServiceDisconnected(ComponentName className) {
                    microRuntimeServiceBinder = null;
                    Log.d(TAG, "initiateService(): Gateway unbound from MicroRuntimeService");
                }
            };
            Log.d(TAG, "initiateService(): Binding Gateway to MicroRuntimeService...");
            bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE);
        } else {
            Log.d(TAG, "initiateService(): MicroRumtimeGateway already binded to service");
            startContainer(profile);
        }
    }

    private void startContainer(Properties profile) {
        if (!MicroRuntime.isRunning()) {
            microRuntimeServiceBinder.startAgentContainer(profile,
                    new RuntimeCallback<Void>() {
                        @Override
                        public void onSuccess(Void thisIsNull) {
                            Log.d(TAG, "startContainer(): Successfully start of the container...");
                            startAgent(NICK_NAME);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.d(TAG, "startContainer(): Failed to start the container..."
                                    + throwable.getMessage() + ", cause: " + throwable.getCause());
                            throwable.printStackTrace();
                            failed();
                        }
                    });
        } else {
            startAgent(NICK_NAME);
        }
    }

    private void startAgent(final String nickname) {
        microRuntimeServiceBinder.startAgent(
                nickname,
                InitialAgent.class.getName(),
                new Object[] { getApplicationContext() },
                new RuntimeCallback<Void>() {
                    @Override
                    public void onSuccess(Void thisIsNull) {
                        Log.d(TAG, "startAgent(): Successfully start of the"
                                + InitialAgent.class.getName() + "...");
                        try {
                            agentStartupCallback.onSuccess(MicroRuntime
                                    .getAgent(nickname));
                        } catch (ControllerException e) {
                            // Should never happen
                            Log.d(TAG, "This should never happen: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.d(TAG, "onFailure: Failed to start the"
                                + InitialAgent.class.getName() + "...");
                        failed();
                    }
                });
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: " + action);
            if (action.equals(LOGIN)) {
                Log.d(TAG, "onReceive: starting LoginActicity");
                Intent showLogin = new Intent(MainActivity.this, LoginActivity.class);
                showLogin.putExtra("nickname", NICK_NAME);
                startActivity(showLogin);
            }
        }
    }
    private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
        @Override
        public void onSuccess(AgentController agent) {

        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.d(TAG, "onFailure: name already in use");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {

        super.onStop();
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: destroying");
        unbindService(serviceConnection);
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
