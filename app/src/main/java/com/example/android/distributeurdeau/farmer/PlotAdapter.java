package com.example.android.distributeurdeau.farmer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.models.Plot;

import java.util.Vector;

public class PlotAdapter extends RecyclerView.Adapter<PlotAdapter.ViewHolder> {

    private Vector<Plot> plots;
    private PlotClickListener listener;

    PlotAdapter(Vector<Plot> plots, PlotClickListener listener) {
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plotNameTV = itemView.findViewById(R.id.plotNameTV);
            itemView.setOnClickListener(this);
        }


        public void bind(int i) {
            Plot plot = plots.get(i);
            plotNameTV.setText(plot.getP_name());
        }

        @Override
        public void onClick(View v) {
            if (listener != null) listener.onClick(getAdapterPosition());
        }
    }

    interface PlotClickListener {
        void onClick(int i);
    }
}
