package com.example.android.distributeurdeau.supervisor;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.android.distributeurdeau.ListItemClickListener;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.farmer.PlotAdapter;
import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Plot;

public class AnalyseFarmerActivity extends AppCompatActivity implements ListItemClickListener {
    private static final String TAG = "AnalyseFarmerActivity";

    private Farmer farmer;
    private PlotAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer);

        FloatingActionButton fab = findViewById(R.id.addPlotFAB);
        fab.hide();

        farmer = (Farmer) getIntent().getSerializableExtra(Strings.EXTRA_FARMER);

        Log.d(TAG, "onCreate: farmer: " + farmer.getPlots().toString());

        adapter = new PlotAdapter(farmer.getPlots(), this);
        RecyclerView recyclerView = findViewById(R.id.farmerRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

    }

    @Override
    public void onClick(int i) {
        Plot plot = farmer.getPlots().get(i);
        Intent intent = new Intent(this, AnalysePlotActivity.class);
        intent.putExtra(Strings.EXTRA_PLOT, plot);
        startActivity(intent);
    }

}
