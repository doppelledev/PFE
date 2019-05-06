package com.example.android.distributeurdeau.farmer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.MainActivity;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Plot;

import java.sql.Date;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jade.android.MicroRuntimeService;
import jade.core.MicroRuntime;

public class AddPlotActivity extends AppCompatActivity {

    private static final String TAG = "AddPlotActivity";
    private static final Pattern typePattern = Pattern.compile("^[a-zA-Zéèàç ]{3,14}$");
    private static final Pattern namePattern = Pattern.compile("^[a-zA-Z0-9éèàç]{3,14}$");

    private FarmerInterface farmerInterface;
    private Farmer farmer;
    private Plot plot;
    private Date date;
    private DatePickerDialog.OnDateSetListener calListener;
    private Receiver receiver;

    private EditText plotNameET;
    private EditText typeET;
    private EditText areaET;
    private TextView dateTV;
    private EditText qteET;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plot);

        farmer = (Farmer) getIntent().getSerializableExtra(Strings.EXTRA_FARMER);

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);

        try {
            farmerInterface = MicroRuntime.getAgent(Strings.FARMER_PREFIX + farmer.getFarmer_num())
                    .getO2AInterface(FarmerInterface.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: error: " + e);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_MODIFICATION_FAILED);
        filter.addAction(Strings.ACTION_MODIFICATION_SUCCEEDED);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

        setupViews();
    }

    private void setupViews() {
        plotNameET = findViewById(R.id.plotNameET);
        typeET = findViewById(R.id.typeTV);
        areaET = findViewById(R.id.areaTV);
        dateTV = findViewById(R.id.dateTV);
        qteET = findViewById(R.id.qteTV);
        progressBar = findViewById(R.id.farmerPB);
        progressBar.setVisibility(View.GONE);

        Button editB = findViewById(R.id.editB);
        editB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendar();
            }
        });

        calListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date = new Date(year - 1900, month, dayOfMonth);
                dateTV.setText(formatDate(date));
            }
        };

        Button saveB = findViewById(R.id.saveB);
        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptToSave();
            }
        });
    }

    private void attemptToSave() {
        progressBar.setVisibility(View.VISIBLE);

        // Get user input and validate it
        final String name = plotNameET.getText().toString();
        if (!validateName(name)) {
            invalid(getString(R.string.toast_invalid_name));
            return;
        }

        final String type = typeET.getText().toString();
        if (!validateType(type)) {
            invalid(getString(R.string.toast_invalid_type));
            return;
        }
        final String area = areaET.getText().toString();
        if (area.isEmpty()) {
            invalid(getString(R.string.toast_invalid_area));
            return;
        }
        final String qte = qteET.getText().toString();
        if (qte.isEmpty()) {
            invalid(getString(R.string.toast_invalid_qte));
            return;
        }
        if (date == null) {
            invalid(getString(R.string.toast_invalid_date));
            return;
        }

        plot = new Plot(farmer, name, type, Float.valueOf(area), Float.valueOf(qte), date);
        // Tell the agent to save user input
        farmerInterface.addPlot(plot);
    }

    private void success() {
        Toast.makeText(this, getString(R.string.toast_modifications_saved), Toast.LENGTH_SHORT).show();
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Strings.EXTRA_PLOT, plot);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void failure() {
        Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
    }

    private String formatDate(Date date) {
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        int day = date.getDate();
        return (day + "-" + month + "-" + year);
    }

    private boolean validateType(String type) {
        Matcher matcher = typePattern.matcher(type);
        return matcher.matches();
    }

    private boolean validateName(String name) {
        Matcher matcher = namePattern.matcher(name);
        return matcher.matches();
    }


    private void invalid(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }

    private void showCalendar() {
        // show a calendar for the user to pick a date
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                AddPlotActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                calListener,
                year, month, day
        );
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: destroying");
        unregisterReceiver(receiver);
        unbindService(MainActivity.serviceConnection);
        super.onDestroy();
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.GONE);
            final String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(Strings.ACTION_MODIFICATION_SUCCEEDED)) {
                success();
            } else {
                failure();
            }
        }
    }
}
