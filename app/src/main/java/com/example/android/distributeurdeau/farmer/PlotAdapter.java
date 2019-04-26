package com.example.android.distributeurdeau.farmer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.distributeurdeau.ListItemClickListener;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.models.Plot;

import java.util.Vector;

public class PlotAdapter extends RecyclerView.Adapter<PlotAdapter.ViewHolder> {
    private static final String TAG = "PlotAdapter";

    private Vector<Plot> plots;
    private ListItemClickListener listener;

    public PlotAdapter(Vector<Plot> plots, ListItemClickListener listener) {
        this.plots = plots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plot_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return plots == null ? 0 : plots.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(i);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView plotNameTV;
        private FrameLayout plotStatusF;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plotNameTV = itemView.findViewById(R.id.plotNameTV);
            plotStatusF = itemView.findViewById(R.id.plotStatusF);
            itemView.setOnClickListener(this);
        }


        public void bind(int i) {
            Plot plot = plots.get(i);
            plotNameTV.setText(plot.getP_name());
            Log.d(TAG, "bind: status " + plot.getStatus() + ", name: " + plot.getP_name());
            switch (plot.getStatus()) {
                case 0:
                    plotStatusF.setBackgroundResource(R.drawable.gray_dot);
                    break;
                case 1:
                    plotStatusF.setBackgroundResource(R.drawable.green_dot);
                    break;
                case 2:
                    plotStatusF.setBackgroundResource(R.drawable.blue_dot);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            if (listener != null) listener.onClick(getAdapterPosition());
        }
    }

}
