package com.example.android.distributeurdeau;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    private EditText hostET;
    private EditText portET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(getString(R.string.settings));

        SharedPreferences sharedPref = getSharedPreferences(
                Strings.NETWORK_SETTINGS, Context.MODE_PRIVATE);
        String host = sharedPref.getString("host", "");
        String port = sharedPref.getString("port", "");

        hostET = findViewById(R.id.hostET);
        portET = findViewById(R.id.portET);
        hostET.setText(host);
        portET.setText(port);


        Button confirmB = findViewById(R.id.confirmB);
        confirmB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = SettingsActivity.this;
                SharedPreferences sharedPref = context.getSharedPreferences(
                        Strings.NETWORK_SETTINGS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                String host =  hostET.getText().toString();
                String port = portET.getText().toString();
                if (!(new FormatValidator()).validateHost(host)) {
                    Toast.makeText(context, "Invalid host", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(new FormatValidator()).validatePort(port)) {
                    Toast.makeText(context, "Invalid port", Toast.LENGTH_SHORT).show();
                    return;
                }
                editor.putString("host", host);
                editor.putString("port", port);
                editor.apply();

                Toast.makeText(context, "Confirmé", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    class FormatValidator {

        private Pattern pattern;
        private Matcher matcher;

        private static final String HOST_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        private static final String PORT_PATTERN =
                "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]" +
                        "{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

        public boolean validateHost(final String host){
            pattern = Pattern.compile(HOST_PATTERN);
            matcher = pattern.matcher(host);
            return matcher.matches();
        }

        public boolean validatePort(final String port){
            pattern = Pattern.compile(PORT_PATTERN);
            matcher = pattern.matcher(port);
            return matcher.matches();
        }
    }
}
