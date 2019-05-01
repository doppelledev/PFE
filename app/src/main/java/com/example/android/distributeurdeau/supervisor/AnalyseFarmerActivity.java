package com.example.android.distributeurdeau.supervisor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.android.distributeurdeau.ListItemClickListener;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.constants.Database;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.farmer.PlotAdapter;
import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Plot;

public class AnalyseFarmerActivity extends AppCompatActivity implements ListItemClickListener {
    private static final String TAG = "AnalyseFarmerActivity";

    private Farmer farmer;
    private PlotAdapter adapter;
    private Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer);

        FloatingActionButton fab = findViewById(R.id.addPlotFAB);
        fab.hide();

        farmer = (Farmer) getIntent().getSerializableExtra(Strings.EXTRA_FARMER);

        Log.d(TAG, "onCreate: farmer: " + farmer.getPlots().toString());

        adapter = new PlotAdapter(farmer.getPlots(), this, false);
        RecyclerView recyclerView = findViewById(R.id.farmerRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);


        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_ACCEPT_SUCCEEDED);
        filter.addAction(Strings.ACTION_NOTIFY);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onClick(int i) {
        Plot plot = farmer.getPlots().get(i);
        Intent intent = new Intent(this, AnalysePlotActivity.class);
        intent.putExtra(Strings.EXTRA_PLOT, plot);
        startActivity(intent);
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            switch (action) {
                case Strings.ACTION_ACCEPT_SUCCEEDED:
                    String pname = intent.getStringExtra(Database.p_name);
                    Log.d(TAG, "onReceive: " + pname);
                    int index = getPlotIndexByName(pname);
                    Plot proposed = (Plot) intent.getSerializableExtra(Strings.EXTRA_PLOT);
                    if (proposed != null) {
                        proposed.proposed = null;
                        proposed.setStatus(2);
                        farmer.getPlots().set(index, proposed);
                    } else {
                        farmer.getPlots().get(index).setStatus(2);
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case Strings.ACTION_NOTIFY:
                    boolean isSend = intent.getBooleanExtra(Strings.EXTRA_BOOLEAN, false);
                    Plot notifPlot = (Plot)intent.getSerializableExtra(Strings.EXTRA_PLOT);
                    if (isSend) {
                        farmer.getPlots().add(notifPlot);
                        adapter.notifyDataSetChanged();
                    } else {
                        int index1 = getPlotIndexByName(notifPlot.getP_name());
                        farmer.getPlots().remove(index1);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }



    private int getPlotIndexByName(String pname) {
        int i;
        for (i = 0; i < farmer.getPlots().size(); i++)
            if (farmer.getPlots().get(i).getP_name().equals(pname))
                break;
        if (i < farmer.getPlots().size())
            return i;
        return -1;
    }

}
