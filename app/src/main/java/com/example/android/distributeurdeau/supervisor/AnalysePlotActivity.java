package com.example.android.distributeurdeau.supervisor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.Strings;
import com.example.android.distributeurdeau.models.Plot;

import java.sql.Date;

public class AnalysePlotActivity extends AppCompatActivity {
    private static final String TAG = "AnalysePlotActivity";

    private Plot plot;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse_plot);

        plot = (Plot) getIntent().getSerializableExtra(Strings.EXTRA_PLOT);

        setupViews();
        populateViews();
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
                refuse();
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
}
