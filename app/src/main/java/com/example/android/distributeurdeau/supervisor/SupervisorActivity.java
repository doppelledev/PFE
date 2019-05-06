package com.example.android.distributeurdeau.supervisor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.distributeurdeau.ListItemClickListener;
import com.example.android.distributeurdeau.MainActivity;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Plot;
import com.example.android.distributeurdeau.models.Supervisor;

import jade.android.MicroRuntimeService;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.NotFoundException;

public class SupervisorActivity extends AppCompatActivity implements ListItemClickListener {
    private static final String TAG = "SupervisorActivity";

    public static SupervisorInterface supervisorInterface;
    private Supervisor supervisor;
    private FarmerAdapter adapter;
    private Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor);
        setTitle(R.string.supervisor);

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class),
                MainActivity.serviceConnection,
                Context.BIND_AUTO_CREATE);

        supervisor = (Supervisor) getIntent().getSerializableExtra(Strings.EXTRA_SUPERVISOR);
        // Get the interface to communicate with the agent
        try {
            supervisorInterface = MicroRuntime.getAgent(Strings.SUPERVISOR_PREFIX + supervisor.getId())
                    .getO2AInterface(SupervisorInterface.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: error: " + e);
        }


        IntentFilter filter = new IntentFilter();
        filter.addAction(Strings.ACTION_NOTIFY);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

        adapter = new FarmerAdapter(supervisor.getFarmers(), this);
        RecyclerView recyclerView = findViewById(R.id.supervisorRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onClick(int i) {
        Farmer farmer = supervisor.getFarmers().get(i);
        Intent intent = new Intent(this, AnalyseFarmerActivity.class);
        intent.putExtra(Strings.EXTRA_FARMER, farmer);
        startActivity(intent);
        Log.d(TAG, "onClick: clicked + " + farmer.getL_name());
    }

    private void logout() {
        try {
            Log.d(TAG, "logout: killing agent");
            // kill agent
            MicroRuntime.killAgent(Strings.SUPERVISOR_PREFIX + supervisor.getId());
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


    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            switch (action) {
                case Strings.ACTION_NOTIFY:
                    int state = intent.getIntExtra(Strings.EXTRA_INT, -1);
                    Plot notifPlot = (Plot)intent.getSerializableExtra(Strings.EXTRA_PLOT);
                    String farmerNum = notifPlot.getFarmer().getFarmer_num();
                    int findex = getFarmerIndexByNum(farmerNum);
                    Farmer farmer = supervisor.getFarmers().get(findex);
                    switch (state) {
                        case 0:
                            farmer.getPlots().add(notifPlot);
                            break;
                        case 1:
                            String pname = notifPlot.getP_name();
                            int pindex = getPlotIndexByName(pname, farmer);
                            farmer.getPlots().remove(pindex);
                            break;
                        case 2:
                            notifPlot.setStatus(2);
                            int index5 = getPlotIndexByName(notifPlot.getP_name(), farmer);
                            farmer.getPlots().set(index5, notifPlot);
                            adapter.notifyDataSetChanged();
                            break;
                    }
                    break;
            }
        }
    }

    private int getPlotIndexByName(String pname, Farmer farmer) {
        int i;
        for (i = 0; i < farmer.getPlots().size(); i++)
            if (farmer.getPlots().get(i).getP_name().equals(pname))
                break;
        if (i < farmer.getPlots().size())
            return i;
        return -1;
    }

    private int getFarmerIndexByNum(String fnum) {
        int i;
        for (i = 0; i < supervisor.getFarmers().size(); i++)
            if (supervisor.getFarmers().get(i).getFarmer_num().equals(fnum))
                break;
        if (i < supervisor.getFarmers().size())
            return i;
        return -1;
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
}
