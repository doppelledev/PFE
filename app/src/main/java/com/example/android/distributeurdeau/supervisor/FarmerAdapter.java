package com.example.android.distributeurdeau.supervisor;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.distributeurdeau.ListItemClickListener;
import com.example.android.distributeurdeau.R;
import com.example.android.distributeurdeau.models.Farmer;

import java.util.Vector;

public class FarmerAdapter extends RecyclerView.Adapter<FarmerAdapter.ViewHolder>  {

    private Vector<Farmer> farmers;
    private ListItemClickListener listener;

    FarmerAdapter(Vector<Farmer> farmers, ListItemClickListener listener) {
        this.farmers = farmers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.farmer_list_item, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(i);
    }

    @Override
    public int getItemCount() {
        return farmers == null ? 0 : farmers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView farmerNameTV;
        TextView farmerNumTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            farmerNameTV = itemView.findViewById(R.id.farmerNameTV);
            farmerNumTV = itemView.findViewById(R.id.farmerNumTV);
            itemView.setOnClickListener(this);
        }

        public void bind(int i) {
            Farmer farmer = farmers.get(i);
            String name = farmer.getF_name() + " " + farmer.getL_name();
            farmerNameTV.setText(name);
            farmerNumTV.setText(farmer.getFarmer_num());
        }

        @Override
        public void onClick(View v) {
            if (listener != null) listener.onClick(getAdapterPosition());
        }
    }
}
