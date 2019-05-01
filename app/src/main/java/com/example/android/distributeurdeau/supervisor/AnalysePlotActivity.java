package com.example.android.distributeurdeau.supervisor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.Estimation;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.models.Plot;

import java.sql.Date;

public class AnalysePlotActivity extends AppCompatActivity {
    private static final String TAG = "AnalysePlotActivity";

    private Plot plot;
    private Plot proposedPlot;

    private ProgressBar progressBar;
    private TextView plotNameTV;
    private TextView typeTV;
    private TextView areaTV;
    private TextView dateTV;
    private TextView qteTV;
    private TextView besoinTV;
    private TextView rendementTV;
    private TextView profitTV;
    private Button acceptB;
    private Button refuseB;
    private ImageView approvedIV;
    private Receiver receiver;
    private Estimation estimation;

    private boolean b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse_plot);

        plot = (Plot) getIntent().getSerializableExtra(Strings.EXTRA_PLOT);

        estimation = new Estimation(SupervisorActivity.supervisorInterface.getCultureData());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_PROPOSAL_SENT);
        filter.addAction(Strings.ACTION_PROPOSAL_FAILED);
        filter.addAction(Strings.ACTION_ACCEPT_SUCCEEDED);
        filter.addAction(Strings.ACTION_ACCEPT_FAILED);
        filter.addAction(Strings.ACTION_NOTIFY);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

        setupViews();
        populateViews();
        analyse();
    }

    private void setupViews() {
        progressBar = findViewById(R.id.farmerPB);
        progressBar.setVisibility(View.GONE);
        plotNameTV = findViewById(R.id.plotNameTV);
        typeTV = findViewById(R.id.typeTV);
        areaTV = findViewById(R.id.areaTV);
        dateTV = findViewById(R.id.dateTV);
        qteTV = findViewById(R.id.qteTV);
        besoinTV = findViewById(R.id.besoinTV);
        rendementTV = findViewById(R.id.rendementTV);
        profitTV = findViewById(R.id.profitTV);
        acceptB = findViewById(R.id.acceptB);
        acceptB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });
        refuseB = findViewById(R.id.refuseB);
        refuseB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                SupervisorActivity.supervisorInterface.propose(proposedPlot);
            }
        });

        approvedIV = findViewById(R.id.approvedIV);
        if (plot.getStatus() == 2) {
            acceptB.setVisibility(View.GONE);
            refuseB.setVisibility(View.GONE);
        } else {
            approvedIV.setVisibility(View.GONE);
        }
    }

    private void populateViews() {
        plotNameTV.setText(plot.getP_name());
        typeTV.setText(plot.getType());
        areaTV.setText(String.valueOf(plot.getArea()));
        dateTV.setText(formatDate(plot.getS_date()));
        qteTV.setText(String.valueOf(plot.getWater_qte()));
    }

    void enableAccept(boolean enable) {
        acceptB.setEnabled(enable);
        acceptB.setBackground(getDrawable(enable ? R.drawable.round_bg_green1 : R.drawable.round_bg_gray));
    }

    void enablePropose(boolean enable) {
        refuseB.setEnabled(enable);
        refuseB.setBackground(getDrawable(enable ? R.drawable.round_bg_green1 : R.drawable.round_bg_gray));
    }

    private String formatDate(Date date) {
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        int day = date.getDate();
        return (day + "-" + month + "-" + year);
    }

    private void accept() {
        SupervisorActivity.supervisorInterface.accept(plot.getP_name(), plot.getFarmer().getFarmer_num());
    }


    private void analyse() {
        float besoin = (float) Math.floor(plot.getWater_qte());
        float dotation = (float) Math.floor(plot.getDotation());
        float estimated = (float) Math.floor((estimation.estimateBesoin(plot, plot.getArea())/0.007)*plot.getArea());

        Log.d(TAG, "analyse: besoin " + besoin);
        Log.d(TAG, "analyse: estimated " + estimated);
        if (besoin == estimated) {
            if (besoin > dotation) {
                tweek();
                enablePropose(true);
                enableAccept(false);
            } else {
                enableAccept(true);
                enablePropose(false);
            }
        } else if (besoin > estimated){
            if (estimated > dotation) {
                tweek();
            } else {
                proposedPlot = new Plot(plot);
                proposedPlot.setWater_qte(estimated);
            }
            enablePropose(true);
            enableAccept(false);
        } else {
            if (besoin > dotation) {
                tweek();
            } else {
                if (estimated > dotation) {
                    tweek();
                } else {
                    proposedPlot = new Plot(plot);
                    proposedPlot.setWater_qte(estimated);
                }
            }
        }
    }

    private void tweek() {
        float dotation = (float) Math.floor(plot.getDotation());
        float newArea = (float) Math.sqrt(dotation / (plot.Kc * plot.ET0 - plot.PLUIE) * 0.007f);
        proposedPlot = new Plot(plot);
        proposedPlot.setArea(newArea);
        proposedPlot.setWater_qte(plot.getDotation());
        // TODO : Date de semi
    }

    private void psuccess() {
        Toast.makeText(this, getString(R.string.toast_proposal_sent), Toast.LENGTH_SHORT).show();
    }

    private void asuccess() {
        Toast.makeText(this, getString(R.string.toast_plan_accepted), Toast.LENGTH_SHORT).show();
        acceptB.setVisibility(View.GONE);
        refuseB.setVisibility(View.GONE);
        approvedIV.setVisibility(View.VISIBLE);
        if (proposedPlot != null)
            plot = proposedPlot;
    }

    private void failure() {
        Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
    }

    private void handleNotification(Plot notifPlot) {

    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.GONE);
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case Strings.ACTION_PROPOSAL_SENT:
                    psuccess();
                    break;
                case Strings.ACTION_PROPOSAL_FAILED:
                    failure();
                    break;
                case Strings.ACTION_ACCEPT_SUCCEEDED:
                    asuccess();
                    break;
                case Strings.ACTION_ACCEPT_FAILED:
                    failure();
                    break;
                case Strings.ACTION_NOTIFY:
                    boolean isSend = intent.getBooleanExtra(Strings.EXTRA_BOOLEAN, false);
                    if (!isSend) {
                        Plot notifPlot = (Plot)intent.getSerializableExtra(Strings.EXTRA_PLOT);
                        if (plot.getP_name().equals(notifPlot.getP_name())) {
                            Toast.makeText(
                                    AnalysePlotActivity.this,
                                    getString(R.string.toast_negotiation_canceled),
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }
                    }
                    break;
            }
        }
    }
}
