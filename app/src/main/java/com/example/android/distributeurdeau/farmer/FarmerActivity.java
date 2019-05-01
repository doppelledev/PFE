package com.example.android.distributeurdeau.farmer;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.distributeurdeau.ListItemClickListener;
import com.example.android.distributeurdeau.MainActivity;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Plot;

import jade.android.MicroRuntimeService;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.NotFoundException;

public class FarmerActivity extends AppCompatActivity implements ListItemClickListener {

    private static final String TAG = "FarmerActivity";

    public static Farmer farmer;
    private FarmerInterface farmerInterface;
    private Receiver receiver;
    private PlotAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer);
        setTitle("Parcelles");

        // The current farmer's data
        farmer = (Farmer) getIntent().getSerializableExtra(Strings.EXTRA_FARMER);
        Log.d(TAG, "onCreate: " + farmer);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_STATUS_UPDATE);
        filter.addAction(Strings.ACTION_PLOT_REMOVE);
        filter.addAction(Strings.ACTION_PLOT_CANCEL);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);

        // Get the interface to communicate with the agent
        try {
            farmerInterface = MicroRuntime.getAgent(Strings.FARMER_PREFIX + farmer.getFarmer_num())
                    .getO2AInterface(FarmerInterface.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: error: " + e);
        }

        adapter = new PlotAdapter(farmer.getPlots(), this);
        RecyclerView recyclerView = findViewById(R.id.farmerRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        FloatingActionButton fab = findViewById(R.id.addPlotFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmerActivity.this, AddPlotActivity.class);
                intent.putExtra(Strings.EXTRA_FARMER, farmer);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                Plot plot = (Plot) data.getSerializableExtra(Strings.EXTRA_PLOT);
                farmer.getPlots().add(plot);
                adapter.notifyDataSetChanged();
                Log.d(TAG, "onActivityResult: hello allow me to iintroduce yself");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
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
            // kill agent
            MicroRuntime.killAgent(farmer.getFarmer_num());
            // stop container
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
            // set the boolean to false, because the container was stopped
            MainActivity.containerStarted = false;
            // start MainActivity
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
        unregisterReceiver(receiver);
        unbindService(MainActivity.serviceConnection);
        super.onDestroy();
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            switch (intent.getAction()) {
                case Strings.ACTION_STATUS_UPDATE:
                    String plotName = intent.getStringExtra(Strings.EXTRA_PLOT);
                    int status = intent.getIntExtra(Strings.EXTRA_STATUS, 0);
                    for (Plot plot : farmer.getPlots()) {
                        if (plot.getP_name().equals(plotName))
                            plot.setStatus(status);
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case Strings.ACTION_PLOT_REMOVE:
                    String plotNamae = intent.getStringExtra(Strings.EXTRA_PLOT);
                    int index1 = getPlotIndexByName(plotNamae);
                    if (index1 >= 0) {
                        farmer.getPlots().remove(index1);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case Strings.ACTION_PLOT_CANCEL:
                    String plotNamae2 = intent.getStringExtra(Strings.EXTRA_PLOT);
                    int index2 = getPlotIndexByName(plotNamae2);
                    if (index2 < farmer.getPlots().size()) {
                        Plot plot = farmer.getPlots().get(index2);
                        plot.setStatus(0);
                        plot.proposed = null;
                        plot.isFarmerTurn = true;
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

    @Override
    public void onClick(int i) {
        Intent intent = new Intent(this, PlotActivity.class);
        intent.putExtra(Strings.EXTRA_PLOT, farmer.getPlots().get(i));
        startActivity(intent);
    }
}
