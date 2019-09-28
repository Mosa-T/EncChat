package com.example.encchat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseReference mDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseUser mCurrUser;
    private FirebaseAuth mAuth;

    private CircleImageView dp;
    private TextView mStatus, d_name;
    private ImageButton changeStatusButt;
    private ImageButton changeDpButt;




    private Button logout_but, profile_friends_but,profile_joke_but;

    private static final int GALLERY_PICK = 1;
    private StorageReference mStorageRef;

    private String Uid;

    private ProgressDialog pd;


    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null)
            mDatabase.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();


        mStatus = (TextView) findViewById(R.id.settings_status);
        d_name = (TextView) findViewById(R.id.settings_dname);
        logout_but = (Button) findViewById(R.id.settings_logout_but);
        dp = (CircleImageView) findViewById(R.id.settings_dp);
        changeStatusButt = (ImageButton) findViewById(R.id.settings_edit_status_but);
        changeDpButt = (ImageButton) findViewById(R.id.settings_edit_image_but);
        profile_friends_but = (Button) findViewById(R.id.settings_friends_but);
        profile_joke_but = findViewById(R.id.settings_jokes_but);
        mCurrUser = FirebaseAuth.getInstance().getCurrentUser();
        Uid = mCurrUser.getUid();

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);
        mDatabase.keepSynced(true);


        loadFriendsCount();


        logout_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
                alertDialogBuilder.setTitle("Logout").setMessage("Do you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child("online").setValue(ServerValue.TIMESTAMP);
                        mAuth.signOut();
                        sendToStart();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });

        profile_joke_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent jokeProfileIntent = new Intent(SettingsActivity.this,JokeActivity.class);
                startActivity(jokeProfileIntent);
            }
        });


        changeDpButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"),GALLERY_PICK);
                CropImage.activity()
                       .start(SettingsActivity.this);
            }
        });

        changeStatusButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("statusString",mStatus.getText().toString());
                startActivity(statusIntent);
            }
        });


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                if(!image.equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.loadload).into(dp, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            //Picasso.get().load(image).placeholder(R.drawable.loadload).into(dp);
                            Toast.makeText(getApplicationContext(),"Couldn't load picture",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                d_name.setText(name);
                mStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void sendToStart(){
        Intent start_int = new Intent(SettingsActivity.this, StartActivity.class);
        startActivity(start_int);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                try {
                    uploadImage(resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1,1)
                    .start(this);
        }
    }

    public void loadFriendsCount(){

        mFriendDatabase.child(Uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profile_friends_but.setText("Friends ("+String.valueOf(dataSnapshot.getChildrenCount())+")");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        profile_friends_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friendProfileIntent = new Intent(SettingsActivity.this,UserFriendsActivity.class);
                friendProfileIntent.putExtra("user_id",Uid);
                startActivity(friendProfileIntent);
            }
        });






    }

    private void uploadImage(Uri resultUri) throws IOException {

        pd = new ProgressDialog(this);
        pd.setMessage("Uploading Image..");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        File file = new File(resultUri.getPath());


        Bitmap compressedImageBitmap_thumb = null;
        try {
            compressedImageBitmap_thumb = new Compressor(this)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(75)
                    .compressToBitmap(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap compressedImageBitmap_prof = new Compressor(this)
                .setMaxHeight(400)
                .setMaxWidth(400)
                .setQuality(60)
                .compressToBitmap(file);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageBitmap_thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        compressedImageBitmap_prof.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        final byte[] thumb_data = baos.toByteArray();
        final byte[] bigdata = baos.toByteArray();

        StorageReference filepath = mStorageRef.child("profile_images").child(Uid+".jpg");
        final StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumbs").child(Uid+".jpg");

        UploadTask uploadTaskbig = filepath.putBytes(bigdata);
        Task<Uri> urlTask = uploadTaskbig.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return task.getResult().getStorage().getDownloadUrl();
            }
        })
        .addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    final Uri download_url = task.getResult();
                    final UploadTask uploadTask = thumb_filepath.putBytes(thumb_data);
                    Task<Uri> urLTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>(){
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception{
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }
                            return task.getResult().getStorage().getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>(){
                        @Override
                        public void onComplete(@NonNull Task<Uri> task){
                            if(task.isSuccessful()){
                                Uri thumb_downloadUrl = task.getResult();


                                Map update_map = new HashMap();
                                update_map.put("image",download_url.toString());
                                update_map.put("thumb_image",thumb_downloadUrl.toString());

                                mDatabase.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            pd.dismiss();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }else{
                    pd.dismiss();
                    Toast.makeText(SettingsActivity.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}