package com.example.android.distributeurdeau;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.distributeurdeau.login.LoginInterface;
import com.example.android.distributeurdeau.models.Farmer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jade.core.MicroRuntime;
import jade.wrapper.ControllerException;

public class RegisterActivity extends AppCompatActivity {

    private static final Pattern namePattern = Pattern.compile("^[a-zA-Zéèàç]{3,14}$");
    private static final Pattern numPattern = Pattern.compile("^[a-zA-Z0-9]{6,14}$");
    private static final Pattern passPattern = Pattern.compile("^[a-zA-Z0-9_*-]{6,20}$");

    private LoginInterface loginInterface;

    private EditText lnameET;
    private EditText fnameET;
    private EditText farmNumET;
    private EditText passET;
    private EditText confPassET;
    private CheckBox riskCB;
    private Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(getString(R.string.register));

        // Get the interface to communicate with the agent
        try {
            loginInterface = MicroRuntime.getAgent(MainActivity.INITIAL_AGENT_NAME)
                    .getO2AInterface(LoginInterface.class);
        } catch (ControllerException e) {
            e.printStackTrace();
            finish();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_REGISTRATION_FAILED);
        filter.addAction(Strings.ACTION_REGISTRATION_SUCCEEDED);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

        setupViews();
    }

    private void setupViews() {
        lnameET = findViewById(R.id.lnameET);
        fnameET = findViewById(R.id.fnameET);
        farmNumET = findViewById(R.id.farmNumET);
        passET = findViewById(R.id.passET);
        confPassET = findViewById(R.id.confPassET);
        riskCB = findViewById(R.id.riskCB);
        Button registerB = findViewById(R.id.registerB);

        registerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
    }

    private void attemptRegister() {
        // Get user input and validate it
        final String fname = fnameET.getText().toString();
        if (!validateName(fname)) {
            showToast(getString(R.string.toast_invalid_fname));
            return;
        }
        final String lname = lnameET.getText().toString();
        if (!validateName(lname)) {
            showToast(getString(R.string.toast_invalid_lname));
            return;
        }
        final String num = farmNumET.getText().toString();
        if (!validateNum(num)) {
            showToast(getString(R.string.toast_invalid_number));
            return;
        }
        final String pass = passET.getText().toString();
        if (!validatePass(pass)) {
            showToast(getString(R.string.toast_invalid_pass));
            return;
        }
        final String conf = confPassET.getText().toString();
        if (!pass.equals(conf)) {
            showToast(getString(R.string.toast_invalid_pass));
            return;
        }
        final Farmer farmer = new Farmer(num, fname, lname, pass, riskCB.isChecked());
        // Tell the agent to save user input
        loginInterface.register(farmer);
    }

    private boolean validateName(String name) {
        Matcher matcher = namePattern.matcher(name);
        return matcher.matches();
    }

    private boolean validateNum(String num) {
        Matcher matcher = numPattern.matcher(num);
        return matcher.matches();
    }

    private boolean validatePass(String pass) {
        Matcher matcher = passPattern.matcher(pass);
        return matcher.matches();
    }

    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            String msg;
            if (action == null)
                return;
            if (intent.getAction().equals(Strings.ACTION_REGISTRATION_SUCCEEDED)) {
                msg = getString(R.string.toast_account_created);
                finish();
            } else {
                msg = getString(R.string.toast_error);
            }
            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
