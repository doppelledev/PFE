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

import com.example.android.distributeurdeau.models.Farmer;

import jade.core.MicroRuntime;
import jade.wrapper.ControllerException;

public class RegisterActivity extends AppCompatActivity {

    private LoginInterface loginInterface;

    private EditText lnameET;
    private EditText fnameET;
    private EditText farmNumET;
    private EditText passET;
    private EditText confPassET;
    private CheckBox riskCB;
    private Button registerB;
    private Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("S'inscrire");

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

        lnameET = findViewById(R.id.lnameET);
        fnameET = findViewById(R.id.fnameET);
        farmNumET = findViewById(R.id.farmNumET);
        passET = findViewById(R.id.passET);
        confPassET = findViewById(R.id.confPassET);
        riskCB = findViewById(R.id.riskCB);
        registerB = findViewById(R.id.registerB);

        registerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fname = fnameET.getText().toString();
                if (fname.isEmpty()) {
                    showToast("Prénom invalide");
                    return;
                }
                final String lname = lnameET.getText().toString();
                if (fname.isEmpty()) {
                    showToast("Nom invalidee");
                    return;
                }
                final String num = farmNumET.getText().toString();
                if (fname.isEmpty()) {
                    showToast("Numéro d'agriculteur invalid");
                    return;
                }
                final String pass = passET.getText().toString();
                if (fname.isEmpty()) {
                    showToast("Mot de passe invalid");
                    return;
                }
                final String conf = passET.getText().toString();
                if (!pass.equals(conf)) {
                    showToast("Mot de passe invalid");
                    return;
                }

                final Farmer farmer = new Farmer(fname, lname, num, pass, riskCB.isChecked());
                loginInterface.register(farmer);
            }
        });
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
            String msg;
            if (intent.getAction().equals(Strings.ACTION_REGISTRATION_SUCCEEDED)) {
                msg = "Compte crée";
                finish();
            } else {
                msg = "Erreur";
            }
            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
