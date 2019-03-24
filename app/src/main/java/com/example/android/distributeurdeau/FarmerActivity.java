package com.example.android.distributeurdeau;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Plot;

import java.util.Calendar;
import java.sql.Date;
import java.util.Vector;

import jade.android.MicroRuntimeService;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.NotFoundException;

public class FarmerActivity extends AppCompatActivity {

    private static final String TAG = "FarmerActivity";

    private Farmer farmer;
    private FarmerInterface farmerInterface;

    private Spinner plotS;
    private EditText typeET;
    private EditText areaET;
    private EditText qteET;
    private TextView dateTV;

    private Date date;
    private Receiver receiver;
    private ProgressBar farmerPB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer);

        farmer = (Farmer) getIntent().getSerializableExtra("farmer");
        Log.d(TAG, "onCreate: farmer: " + farmer.getPlots().get(0).toString());

        IntentFilter filter = new IntentFilter();
        filter.addAction("success");
        filter.addAction("failure");
        receiver = new Receiver();
        registerReceiver(receiver, filter);

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);
        try {
            farmerInterface =
                    MicroRuntime
                            .getAgent(farmer.getFarmer_num())
                            .getO2AInterface(FarmerInterface.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: error: " + e);
        }

        farmerPB = findViewById(R.id.farmerPB);
        farmerPB.setVisibility(View.GONE);

        plotS = findViewById(R.id.plotS);
        typeET = findViewById(R.id.typeET);
        areaET = findViewById(R.id.areaET);
        dateTV = findViewById(R.id.dateTV);
        qteET = findViewById(R.id.qteET);

        Button saveB = findViewById(R.id.saveB);
        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                farmerPB.setVisibility(View.VISIBLE);
                final String type = typeET.getText().toString();
                if (type.isEmpty()) {
                    return;
                }
                final String area = areaET.getText().toString();
                if (area.isEmpty()) {
                    return;
                }
                final String qte = qteET.getText().toString();
                if (qte.isEmpty()) {
                    return;
                }
                Plot plot = farmer.getPlots().get(plotS.getSelectedItemPosition());
                plot.setType(type);
                plot.setArea(Float.valueOf(area));
                plot.setWater_qte(Float.valueOf(qte));
                plot.setS_date(date);
                Log.d(TAG, "onClick: " + plot);
                farmerInterface.modifyPlot(plot);
            }
        });

        final DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date = new Date(year-1900, month, dayOfMonth);
                month = month + 1;
                dateTV.setText(dayOfMonth + "-" + month + "-" + year);
            }
        };

        Button editB = findViewById(R.id.editB);
        editB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        FarmerActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        listener,
                        year, month, day
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        populateSpinner();
    }

    private void populateSpinner() {
        // Plots names spinner
        Vector<Plot> plots = farmer.getPlots();
        String [] plotsNames;
        if (plots.size() > 0) {
            plotsNames = new String[plots.size()];
            for (int i =0; i < plots.size(); i++) {
                plotsNames[i] = plots.get(i).getP_name();
            }
        } else {
            plotsNames = new String[] {"Vide"};
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, plotsNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plotS.setAdapter(adapter);

        plotS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshFields(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void refreshFields(int index) {
        final Plot plot = farmer.getPlots().get(index);
        typeET.setText(plot.getType());
        areaET.setText(String.valueOf(plot.getArea()));
        date = plot.getS_date();
        final String dateStr = (date.getDay() + 1) + "-" + (date.getMonth() + 1) + "-" + (date.getYear() + 1900);
        dateTV.setText(dateStr);
        qteET.setText(String.valueOf(plot.getWater_qte()));
    }

    private class Receiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            farmerPB.setVisibility(View.GONE);
            final String action = intent.getAction();
            if (action.equals("success")) {
                success();
            } else {
                failure();
            }
        }
    }

    private void success() {
        Toast.makeText(this, "Modifications enregistrés", Toast.LENGTH_SHORT).show();
    }

    private void failure() {
        Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
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
            MainActivity.microRuntimeServiceBinder.stopAgentContainer(
                    new RuntimeCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: stopped container");
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.d(TAG, "onSuccess: couldn't stop container");

                        }
                    }
            );
            MainActivity.b = true;
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

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: destroying");
        unbindService(MainActivity.serviceConnection);
        super.onDestroy();
    }
}
