package com.phenamwema.travelmantics;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserActivityRecyclerView extends RecyclerView.Adapter<UserActivityRecyclerView.ViewHolder> {

    ArrayList<TravelDeal> listDeals;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;
    UserActivity context;
    private ImageView dealImage;

    public UserActivityRecyclerView(){
        FirebaseUtil.Reference("traveldeals",context);
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        listDeals = FirebaseUtil.listDeals;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);
                listDeals.add(travelDeal);
                notifyItemInserted(listDeals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(childEventListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.travel_card_layout,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserActivityRecyclerView.ViewHolder viewHolder, int i) {
        TravelDeal travelDeal = listDeals.get(i);
        viewHolder.bind(travelDeal);
    }

    @Override
    public int getItemCount() {
        return listDeals.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        TextView title, description,price;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            description = itemView.findViewById(R.id.tvDescription);
            price = itemView.findViewById(R.id.tvPrice);
            //imageResort = itemView.findViewById(R.id.imageResort);
            dealImage = (ImageView) itemView.findViewById(R.id.imageResort);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal travelDeal) {
            title.setText(travelDeal.getTitle());
            description.setText(travelDeal.getDescription());
            price.setText(travelDeal.getPrice());
            showImage(travelDeal.getImageUrl());
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TravelDeal selectDeal = listDeals.get(position);
            Intent intent = new Intent(view.getContext(),AdminActivity.class);
            intent.putExtra("Deal",selectDeal);
            view.getContext().startActivity(intent);
        }


        private void showImage(String url){
            if(url != null && url.isEmpty()==false){
                Picasso.with(itemView.getContext())
                        .load(url)
                        .resize(300,300)
                        .centerCrop()
                        .into(dealImage);
            }
        }
    }
}
