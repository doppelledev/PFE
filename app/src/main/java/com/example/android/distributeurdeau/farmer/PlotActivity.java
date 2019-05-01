package com.example.android.distributeurdeau.farmer;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.Estimation;
import com.example.android.distributeurdeau.MainActivity;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.models.Plot;

import java.sql.Date;
import java.text.DecimalFormat;
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
    private Estimation estimation;

    private EditText typeET;
    private EditText areaET;
    private EditText qteET;
    private TextView dateTV;
    private ProgressBar farmerPB;
    private Button button1;
    private Button button2;
    private Button editB;

    private TextView besoinTV;
    private TextView rendementTV;
    private TextView profitTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        // The current farmer's data
        plot = (Plot) getIntent().getSerializableExtra(Strings.EXTRA_PLOT);
        Log.d(TAG, "onCreate: " + plot);
        setTitle(plot.getP_name());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_MODIFICATION_FAILED);
        filter.addAction(Strings.ACTION_MODIFICATION_SUCCEEDED);
        filter.addAction(Strings.ACTION_SEND_FAILED);
        filter.addAction(Strings.ACTION_SEND_SUCCEEDED);
        filter.addAction(Strings.ACTION_DELETE_SUCCEEDED);
        filter.addAction(Strings.ACTION_CANCEL_FAILED);
        filter.addAction(Strings.ACTION_CANCEL_SUCCEEDED);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);

        // Get the interface to communicate with the agent
        try {
            farmerInterface = MicroRuntime.getAgent(Strings.FARMER_PREFIX + plot.getFarmer().getFarmer_num())
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

        estimation = new Estimation(farmerInterface.getCultureData());

        setupViews();
        showOriginal(true);
    }

    private void setupViews() {
        farmerPB = findViewById(R.id.farmerPB);
        farmerPB.setVisibility(View.GONE);

        typeET = findViewById(R.id.typeTV);
        areaET = findViewById(R.id.areaTV);
        dateTV = findViewById(R.id.dateTV);
        qteET = findViewById(R.id.qteTV);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);

        editB = findViewById(R.id.editB);
        editB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendar();
            }
        });



        if (plot.getStatus() == 2) {
            button1.setEnabled(false);
            button1.setBackgroundColor(Color.GRAY);
            button2.setEnabled(false);
            button2.setBackgroundColor(Color.GRAY);
        }

        besoinTV = findViewById(R.id.besoinTV);
        profitTV = findViewById(R.id.profitTV);
        rendementTV = findViewById(R.id.rendementTV);

        RadioButton proposed = findViewById(R.id.proposedRadio);
        if (plot.proposed == null)
            proposed.setEnabled(false);


        populateViews(plot);
    }

    private void showOriginal(boolean b) {
        if (b) {
            populateViews(plot);
            button1.setText(getString(R.string.send));
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send();
                }
            });

            button2.setText(getString(R.string.cancel));
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel();
                }
            });
            if (plot.getStatus() == 0) {
                enableButton1(true);
                enableButton2(false);
            } else if (plot.getStatus() == 1){
                enableButton1(false);
                enableButton2(true);
            } else {
                enableButton1(false);
                enableButton2(false);
            }
            enableViews(plot.getStatus() == 0);
        } else {
            populateViews(plot.proposed);
            button1.setText(getString(R.string.accept));
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: accept proposed
                }
            });

            button2.setText(getString(R.string.refuse));
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: refuse proposed
                }
            });
            enableButton1(true);
            enableButton2(true);
            // TODO: set on click listeners
            enableViews(false);
        }
    }

    private void enableButton1(boolean enable) {
        button1.setEnabled(enable);
        button1.setBackground(getDrawable(enable ? R.drawable.round_bg_green1 : R.drawable.round_bg_gray));
    }

    private void enableButton2(boolean enable) {
        button2.setEnabled(enable);
        button2.setBackground(getDrawable(enable ? R.drawable.round_bg_green1 : R.drawable.round_bg_gray));
    }

    private void populateViews(Plot plot) {
        typeET.setText(plot.getType());
        areaET.setText(String.valueOf(plot.getArea()));
        dateTV.setText(formatDate(plot.getS_date()));
        qteET.setText(String.valueOf(plot.getWater_qte()));

        updateEstimationFields(plot.getArea());
        areaET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0)
                    updateEstimationFields(Float.valueOf(String.valueOf(s)));
                else
                    emptyEstimationFields();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void emptyEstimationFields() {
        besoinTV.setText("0 m3");
        rendementTV.setText("0 q/ha");
        profitTV.setText("0 Dh");
    }

    private void updateEstimationFields(float area) {
        DecimalFormat f = new DecimalFormat("#.##");
        String besoin = String.valueOf(f.format((estimation.estimateBesoin(plot, area)/0.007)*area))+ " m3";
        String rendement = String.valueOf(f.format(estimation.estimateRendement(plot, area))) + " q/ha";
        String profit = String.valueOf(f.format(estimation.estimateProfit(plot, area))) + " Dh";
        besoinTV.setText(besoin);
        rendementTV.setText(rendement);
        profitTV.setText(profit);
    }

    private void enableViews(boolean enable) {
        typeET.setEnabled(enable);
        areaET.setEnabled(enable);
        qteET.setEnabled(enable);
        editB.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.originalRadio:
                if (checked)
                    showOriginal(true);
                break;
            case R.id.proposedRadio:
                if (checked)
                    showOriginal(false);
                break;
        }
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

        for (Plot p : FarmerActivity.farmer.getPlots()) {
            if (p.getP_name().equals(plot.getP_name())) {
                p.setType(type);
                p.setArea(Float.valueOf(area));
                p.setWater_qte(Float.valueOf(qte));
                if (date != null)
                    p.setS_date(date);
                return;
            }
        }
    }

    private void send() {
        attemptToSave();
        farmerPB.setVisibility(View.VISIBLE);
        farmerInterface.sendPlot(plot.getP_name(), plot.getFarmer().getFarmer_num(), plot.getWater_qte());
    }

    private void cancel() {
        farmerPB.setVisibility(View.VISIBLE);
        farmerInterface.cancelNegotiation(plot.getP_name(), plot.getFarmer().getFarmer_num());
    }

    private boolean validateType(String type) {
        Matcher matcher = typePattern.matcher(type);
        return matcher.matches();
    }

    private void invalid(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        farmerPB.setVisibility(View.GONE);
    }

    private void modificationSuccess() {
        Toast.makeText(this, getString(R.string.toast_modifications_saved), Toast.LENGTH_SHORT).show();
    }

    private void sendSuccess() {
        Toast.makeText(this, getString(R.string.toast_plot_sent), Toast.LENGTH_SHORT).show();
        enableButton1(false);
        enableButton2(true);
        Intent broadcast = new Intent();
        broadcast.setAction(Strings.ACTION_STATUS_UPDATE);
        broadcast.putExtra(Strings.EXTRA_STATUS, 1);
        broadcast.putExtra(Strings.EXTRA_PLOT, plot.getP_name());
        sendBroadcast(broadcast);
    }

    private void deleteSuccess() {
        Toast.makeText(this, getString(R.string.toast_plot_deleted), Toast.LENGTH_SHORT).show();
        Intent broadcast = new Intent();
        broadcast.setAction(Strings.ACTION_PLOT_REMOVE);
        broadcast.putExtra(Strings.EXTRA_PLOT, plot.getP_name());
        sendBroadcast(broadcast);
        finish();
    }

    private void cancelSucceeded() {
        Toast.makeText(this, getString(R.string.toast_negotiation_canceled), Toast.LENGTH_SHORT).show();
        enableButton1(true);
        enableButton2(false);
        plot.setStatus(0);
        plot.proposed = null;
        plot.isFarmerTurn = true;
        Intent broadcast = new Intent();
        broadcast.setAction(Strings.ACTION_PLOT_CANCEL);
        broadcast.putExtra(Strings.EXTRA_PLOT, plot.getP_name());
        sendBroadcast(broadcast);
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
                showDeleteAlert();
                break;
            case R.id.save:
                if (plot.getStatus() == 0) {
                    farmerPB.setVisibility(View.VISIBLE);
                    attemptToSave();
                } else {
                    Toast.makeText(this, getString(R.string.toast_negotiating), Toast.LENGTH_SHORT).show();
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private void showDeleteAlert() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert))
                .setMessage(getString(R.string.delete_plot))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        deletePlot();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deletePlot() {
        farmerPB.setVisibility(View.VISIBLE);
        farmerInterface.deletePlot(plot.getP_name(), plot.getFarmer().getFarmer_num());
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
            switch (action) {
                case Strings.ACTION_MODIFICATION_SUCCEEDED:
                    modificationSuccess();
                    break;
                case Strings.ACTION_SEND_SUCCEEDED:
                    sendSuccess();
                    break;
                case Strings.ACTION_DELETE_SUCCEEDED:
                    deleteSuccess();
                    break;
                case Strings.ACTION_CANCEL_SUCCEEDED:
                    cancelSucceeded();
                    break;
                default:
                    failure();
                    break;
            }
        }
    }
}
