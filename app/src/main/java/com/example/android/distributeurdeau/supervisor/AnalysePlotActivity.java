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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.models.CultureData;
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

    private Receiver receiver;

    private boolean b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse_plot);

        plot = (Plot) getIntent().getSerializableExtra(Strings.EXTRA_PLOT);


        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_PROPOSAL_SENT);
        filter.addAction(Strings.ACTION_PROPOSAL_FAILED);
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
    }

    private void populateViews() {
        plotNameTV.setText(plot.getP_name());
        typeTV.setText(plot.getType());
        areaTV.setText(String.valueOf(plot.getArea()));
        dateTV.setText(formatDate(plot.getS_date()));
        qteTV.setText(String.valueOf(plot.getWater_qte()));
    }

    void disableAccept() {
        acceptB.setEnabled(false);
        acceptB.setBackground(getDrawable(R.drawable.round_bg_gray));
    }

    void enableAccept() {
        acceptB.setEnabled(true);
        acceptB.setBackground(getDrawable(R.drawable.round_bg_green1));
    }

    void disablePropose() {
        refuseB.setEnabled(false);
        refuseB.setBackground(getDrawable(R.drawable.round_bg_gray));
    }

    void enablePropose() {
        refuseB.setEnabled(true);
        refuseB.setBackground(getDrawable(R.drawable.round_bg_green1));
    }

    private String formatDate(Date date) {
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        int day = date.getDate();
        return (day + "-" + month + "-" + year);
    }

    private void accept() {

    }

    private void refuse() {

    }

    float estimateBesoin() {
        return (plot.Kc * plot.ET0 - plot.PLUIE) * plot.getArea() / 0.007f * plot.getArea();
    }

    float estimateRendement() {
        float etcAdj = (estimateBesoin() + plot.PLUIE) * plot.getArea();
        float etc = plot.Kc * plot.ET0 * plot.getArea();
        return (plot.Ky * (1 - etcAdj / etc) - 1) * plot.Ym * -1;
    }

    float estimateProfit() {
        return estimateRendement() * getPriceFromCultureData() * plot.getArea();
    }

    float getPriceFromCultureData() {
        for (CultureData data : SupervisorActivity.supervisorInterface.getCultureData()) {
            if (data.getName().equals(plot.getType()))
                return data.getPrice();
        }
        return 300.0f;
    }

    private void analyse() {
        float besoin = (float) Math.floor(plot.getWater_qte());
        float dotation = (float) Math.floor(plot.getDotation());
        float estimated = (float) Math.floor(estimateBesoin());

        Log.d(TAG, "analyse: besoin " + besoin);
        Log.d(TAG, "analyse: estimated " + estimated);
        if (besoin == estimated) {
            if (besoin > dotation) {
                float newArea = dotation / (plot.Kc * plot.ET0 - plot.PLUIE);
                proposedPlot = new Plot(plot);
                proposedPlot.setArea(newArea);
                // TODO : Date de semi
                enablePropose();
                disableAccept();
            } else {
                enableAccept();
                disablePropose();
                // TODO: accepter le plan
            }
        } else {

        }
    }

    private void success() {
        Toast.makeText(this, getString(R.string.toast_proposal_sent), Toast.LENGTH_SHORT).show();
    }

    private void failure() {
        Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.GONE);
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case Strings.ACTION_PROPOSAL_SENT:
                    success();
                    break;
                case Strings.ACTION_PROPOSAL_FAILED:
                    failure();
                    break;
            }
        }
    }
}
