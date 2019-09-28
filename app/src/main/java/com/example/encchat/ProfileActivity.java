

package com.example.encchat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView profile_name, profile_status;
    private CircleImageView profile_image;
    private Button friend_req_butt, decline_butt, friends_butt;




    private ProgressDialog pd;

    private int curr_state;

    private DatabaseReference mDatabase;
    private DatabaseReference mUserDatabase;
    private DatabaseReference friendReq_Database;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference root_ref;
    private DatabaseReference notiref;
    private String Uid,user_key;
    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null)
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        pd = new ProgressDialog(this);
        pd.setMessage("Loading user Profile..");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        curr_state = 0;

        user_key = getIntent().getStringExtra("from_user_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_key);
        friendReq_Database = FirebaseDatabase.getInstance().getReference().child("Friendreq");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        root_ref = FirebaseDatabase.getInstance().getReference();
        notiref = root_ref.child("Notifications");
        mUserDatabase = root_ref.child("Users").child(Uid);
        profile_name =(TextView) findViewById(R.id.prof_name);
        profile_status = (TextView) findViewById(R.id.prof_status);
        friend_req_butt = (Button) findViewById(R.id.prof_frreq_but);
        profile_image = (CircleImageView) findViewById(R.id.prof_image);
        decline_butt = (Button) findViewById(R.id.prof_frreq_decline_but);
        friends_butt = (Button) findViewById(R.id.prof_friends_but);

        mDatabase.keepSynced(true);


        if(Uid.equals(user_key))
            friend_req_butt.setVisibility(View.GONE);

        decline_butt.setVisibility(View.GONE);
        friends_butt.setVisibility(View.GONE);
        decline_butt.setEnabled(false);



        loadFriendsCount();


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {




                String display_name=dataSnapshot.child("name").getValue().toString();
                String display_status=dataSnapshot.child("status").getValue().toString();
                String display_image=dataSnapshot.child("image").getValue().toString();
                profile_name.setText(display_name);
                profile_status.setText(display_status);
                if(!display_image.equals("default")) {
                    Picasso.get().load(display_image).placeholder(R.drawable.loadload).into(profile_image);
                } else
                    Picasso.get().load(R.drawable.ic_person_black_24dp);

                friendReq_Database.child(Uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_key)){
                            String req_type = dataSnapshot.child(user_key).child("req_type").getValue().toString();
                            if(req_type.equals("received")){
                                friend_req_butt.setText("Accept Friend Request");
                                curr_state = 2;

                                decline_butt.setVisibility(View.VISIBLE);
                                decline_butt.setEnabled(true);
                            } else if(req_type.equals("sent")){
                                friend_req_butt.setText("Cancel Friend Request");
                                curr_state = 1;

                                decline_butt.setVisibility(View.GONE);
                                decline_butt.setEnabled(false);
                            }
                        }
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                mFriendDatabase.child(Uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_key)){
                            curr_state = 3 ;

                            friend_req_butt.setBackgroundResource(R.drawable.m_colour);
                            friend_req_butt.setTextColor(Color.BLACK);
                            friends_butt.setVisibility(View.VISIBLE);
                            friend_req_butt.setText("Unfriend");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        friends_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friendProfileIntent = new Intent(ProfileActivity.this,UserFriendsActivity.class);
                friendProfileIntent.putExtra("user_id",user_key);
                startActivity(friendProfileIntent);
            }
        });




        //Decline friend request

        decline_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd = new ProgressDialog(ProfileActivity.this);
                pd.setMessage("Declining request..");
                pd.show();


                friendReq_Database.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendReq_Database.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                friend_req_butt.setEnabled(true);
                                curr_state = 0;
                                friend_req_butt.setText("Send Friend Request");
                                decline_butt.setVisibility(View.GONE);
                                decline_butt.setEnabled(false);

                                pd.dismiss();
                            }
                        });
                    }
                });



            }
        });


        friend_req_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                friend_req_butt.setEnabled(false);

                //Send Friend Request

                if(curr_state == 0){


                    Map reqmap = new HashMap();
                    reqmap.put("Friendreq/"+Uid+"/"+user_key+"/req_type","sent");
                    reqmap.put("Friendreq/"+user_key+"/"+Uid+"/req_type","received");

                    final HashMap<String,String> notimap = new HashMap<String, String>();
                    notimap.put("from",Uid);
                    notimap.put("type","request");



                    root_ref.updateChildren(reqmap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            notiref.child(user_key).push().setValue(notimap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friend_req_butt.setEnabled(true);
                                    curr_state = 1;
                                    friend_req_butt.setText("Cancel Friend Request");
                                    Toast.makeText(ProfileActivity.this, "Request sent Succesfully", Toast.LENGTH_SHORT).show();
                                    decline_butt.setVisibility(View.GONE);
                                    decline_butt.setEnabled(false);
                                }
                            });
                        }
                    });

                }

                //Cancel friend Request

                if(curr_state == 1) {
                    friend_req_butt.setEnabled(false);
                    friendReq_Database.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendReq_Database.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friend_req_butt.setEnabled(true);
                                    curr_state = 0;
                                    friend_req_butt.setText("Send Friend Request");

                                    decline_butt.setVisibility(View.GONE);
                                    decline_butt.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                //Accept Friend request

                if (curr_state == 2){

                    final String currDate = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    pd = new ProgressDialog(ProfileActivity.this);
                    pd.setMessage("Accepting Friend Request..");
                    pd.show();


                    Map accfrndmap = new HashMap();
                    accfrndmap.put("Friends/"+Uid+"/"+user_key+"/date",currDate);
                    accfrndmap.put("Friends/"+user_key+"/"+Uid+"/date",currDate);

                    accfrndmap.put("Friendreq/"+Uid+"/"+user_key,null);
                    accfrndmap.put("Friendreq/"+user_key+"/"+Uid,null);


                    root_ref.updateChildren(accfrndmap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            pd.dismiss();
                            friend_req_butt.setEnabled(true);
                            curr_state = 3;
                            friends_butt.setVisibility(View.VISIBLE);
                            friend_req_butt.setText("Unfriend");
                            friend_req_butt.setBackgroundResource(R.drawable.m_colour);
                            friend_req_butt.setTextColor(Color.BLACK);
                            decline_butt.setVisibility(View.GONE);
                            decline_butt.setEnabled(false);
                        }
                    });
                }

                //Unfriend

                if(curr_state==3){

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
                    alertDialogBuilder.setTitle("Unfriend?").setMessage("You wil have to send request to be friends again.Unfriend?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            mFriendDatabase.child(Uid).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_key).child(Uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {


                                            friend_req_butt.setEnabled(true);
                                            curr_state =0;
                                            friend_req_butt.setText("Send Friend Request");
                                            decline_butt.setVisibility(View.GONE);
                                            friends_butt.setVisibility(View.GONE);
                                            decline_butt.setEnabled(false);
                                        }
                                    });
                                }
                            });
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();

                }
            }
        });
    }


    public void loadFriendsCount(){

        mFriendDatabase.child(user_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends_butt.setText("Friends ("+String.valueOf(dataSnapshot.getChildrenCount())+")");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






    }
}