package com.phenamwema.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AdminActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    EditText txtitle, txdescription, txprice;
    TravelDeal deal;
    ImageView imageView;
    UploadTask storageTask;
    private static final int PICTURE_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("traveldeals");

        txtitle = (EditText) findViewById(R.id.title);
        txdescription = (EditText) findViewById(R.id.description);
        txprice = (EditText) findViewById(R.id.price);
        imageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if(deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtitle.setText(deal.getTitle());
        txdescription.setText(deal.getDescription());
        txprice.setText(deal.getPrice());
        showImage(deal.getImageUrl());

        CardView select = (CardView) findViewById(R.id.cardView);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,"Insert picture"),PICTURE_RESULT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            StorageReference reference = FirebaseUtil.storageReference.child(imageUri.getLastPathSegment());
            reference.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri downloadUrl = uri.getResult();
                    deal.setImageUrl(downloadUrl.toString());
                    showImage(downloadUrl.toString());
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save:
                saveDeal();
                Toast.makeText(this,"Deal Saved",Toast.LENGTH_LONG).show();
                clean();
                return true;
            case R.id.delete:
                deleteDeal();
                Toast.makeText(this,"Deal has been deleted",Toast.LENGTH_LONG).show();
                startActivity(new Intent(AdminActivity.this,UserActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete).setVisible(true);
            menu.findItem(R.id.save).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.cardView).setEnabled(true);
        }else{
            menu.findItem(R.id.delete).setVisible(false);
            menu.findItem(R.id.save).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.cardView).setEnabled(false);
        }
        return true;
    }

    private void saveDeal(){
        deal.setTitle(txtitle.getText().toString());
        deal.setPrice(txprice.getText().toString());
        deal.setDescription(txdescription.getText().toString());
        if(deal.getId()==null){
            databaseReference.push().setValue(deal);//insert object into database
        } else{
            databaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal(){
        if(deal==null){
            Toast.makeText(this,"Please save deal before deletion.",Toast.LENGTH_LONG).show();
            return;
        }
        databaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName()!=null&&deal.getImageName().isEmpty()==false){
            StorageReference pictureReference = FirebaseUtil.firebaseStorage.getReference().child(deal.getImageName());
            pictureReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete image","Image deleted");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete image", "Failed to delete");
                }
            });
        }
    }

    private void clean(){
        //clear textviews and set focus to title edittext
        txtitle.setText("");
        txprice.setText("");
        txdescription.setText("");
        imageView.setImageResource(0);
        txtitle.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled){
        txprice.setEnabled(isEnabled);
        txdescription.setEnabled(isEnabled);
        txtitle.setEnabled(isEnabled);
    }

    private void showImage(String url){
        if(url!=null && url.isEmpty() ==false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;//get width of the screen
            Picasso.with(this)
                    .load(url)
                    .resize(width,width*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
