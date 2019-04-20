package com.example.android.distributeurdeau;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.farmer.FarmerInterface;
import com.example.android.distributeurdeau.models.Plot;

import java.sql.Date;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jade.android.MicroRuntimeService;
import jade.core.MicroRuntime;

public class PlotActivity extends AppCompatActivity {

    private static final String TAG = "PlotActivity";
    private static final Pattern typePattern = Pattern.compile("^[a-zA-Zéèàç ]{3,14}$");

    private Plot plot;
    private Date date;
    private FarmerInterface farmerInterface;
    private Receiver receiver;
    private DatePickerDialog.OnDateSetListener calListener;

    private TextView plotNameTV;
    private EditText typeET;
    private EditText areaET;
    private EditText qteET;
    private TextView dateTV;
    private ProgressBar farmerPB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);
        setTitle(getString(R.string.plots));

        // The current farmer's data
        plot = (Plot) getIntent().getSerializableExtra(Strings.EXTRA_PLOT);
        Log.d(TAG, "onCreate: " + plot);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_MODIFICATION_FAILED);
        filter.addAction(Strings.ACTION_MODIFICATION_SUCCEEDED);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);

        // Get the interface to communicate with the agent
        try {
            farmerInterface = MicroRuntime.getAgent(plot.getFarmer().getFarmer_num())
                    .getO2AInterface(FarmerInterface.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: error: " + e);
        }

        // a listener to handle date pick
        calListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date = new Date(year - 1900, month, dayOfMonth);
                dateTV.setText(formatDate(date));
            }
        };

        setupViews();
    }

    private void setupViews() {
        farmerPB = findViewById(R.id.farmerPB);
        farmerPB.setVisibility(View.GONE);

        plotNameTV = findViewById(R.id.plotNameTV);
        typeET = findViewById(R.id.typeET);
        areaET = findViewById(R.id.areaET);
        dateTV = findViewById(R.id.dateTV);
        qteET = findViewById(R.id.qteET);

        Button saveB = findViewById(R.id.saveB);
        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                farmerPB.setVisibility(View.VISIBLE);
                attemptToSave();
            }
        });

        Button editB = findViewById(R.id.editB);
        editB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendar();
            }
        });

        populateViews();
    }

    private void populateViews() {
        plotNameTV.setText(plot.getP_name());
        typeET.setText(plot.getType());
        areaET.setText(String.valueOf(plot.getArea()));
        dateTV.setText(formatDate(plot.getS_date()));
        qteET.setText(String.valueOf(plot.getWater_qte()));
    }

    private String formatDate(Date date) {
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        int day = date.getDate();
        return (day + "-" + month + "-" + year);
    }


    private void showCalendar() {
        // show a calendar for the user to pick a date
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                PlotActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                calListener,
                year, month, day
        );
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void attemptToSave() {
        if (plot.getFarmer().getPlots().size() == 0) {
            invalid(getString(R.string.toast_no_plot));
            return;
        }
        // Get user input and validate it
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
        plot.setType(type);
        plot.setArea(Float.valueOf(area));
        plot.setWater_qte(Float.valueOf(qte));
        if (date != null)
            plot.setS_date(date);
        // Tell the agent to save user input
        farmerInterface.modifyPlot(plot);
    }

    private boolean validateType(String type) {
        Matcher matcher = typePattern.matcher(type);
        return matcher.matches();
    }

    private void invalid(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        farmerPB.setVisibility(View.GONE);
    }

    private void success() {
        Toast.makeText(this, getString(R.string.toast_modifications_saved), Toast.LENGTH_SHORT).show();
    }

    private void failure() {
        Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.plot_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deletePlot();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void deletePlot() {

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
            farmerPB.setVisibility(View.GONE);
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
