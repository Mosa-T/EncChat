package com.example.encchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private ViewPager view_pager;
    private MainPagerAdapter mPa;
    private TabLayout mTabLayout;


    private DatabaseReference user_ref;
    private DatabaseReference pubKey_ref;
    KeyPairGenerator kpg;
    KeyPair kp;
    PublicKey publicKey;
    PrivateKey privateKey;

    public String privateKeyString;
    private String publicKeystring;

    public String MY_PREFS_NAME = "socio_prefs";


    public void genKeys() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{



        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        privateKeyString = prefs.getString("private_key"+mAuth.getCurrentUser().getUid(), null);
        publicKeystring = prefs.getString("public_key"+mAuth.getCurrentUser().getUid(), null);

        if(privateKeyString==null && publicKeystring==null) {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();

            if (privateKey != null) {
                privateKeyString = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
            }
            if (publicKey != null) {
                publicKeystring = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
            }

            pubKey_ref.child(mAuth.getCurrentUser().getUid()).child("pub").setValue(publicKeystring).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("private_key"+mAuth.getCurrentUser().getUid(), privateKeyString);
                    editor.putString("public_key"+mAuth.getCurrentUser().getUid(), publicKeystring);
                    editor.apply();


                }
            });
        }




        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE); //was removed
        String priKey = sharedPref.getString("private_key",null); //was removed

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_appbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.app_name);



        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mAuth = FirebaseAuth.getInstance();

        pubKey_ref = FirebaseDatabase.getInstance().getReference().child("PubKey");

        if(mAuth.getCurrentUser() != null)
            user_ref = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());



        view_pager = (ViewPager) findViewById(R.id.tab_pager);
        mPa = new MainPagerAdapter(getSupportFragmentManager());

        view_pager.setAdapter(mPa);

        mTabLayout.setupWithViewPager(view_pager);






        view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if(mAuth.getCurrentUser()!=null) {
            try {
                genKeys();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
        }

    }



    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser curr_user = mAuth.getCurrentUser();

        if (curr_user != null) {
            user_ref.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser curr_user = mAuth.getCurrentUser();

        if(curr_user == null){
            sendToStart();
        }
    }


    public void sendToStart(){
        Intent start_int = new Intent(MainActivity.this, StartActivity.class);
        startActivity(start_int);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
        switch (id)
        {
            case R.id.profile_but:
                Intent set_intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(set_intent);
                break;
            case  R.id.allusers_but:
                Intent all_users_Intent = new Intent(MainActivity.this,UsersActivity.class);
                startActivity(all_users_Intent);
                break;
            case R.id.listusers:
                Intent set_int = new Intent(MainActivity.this,ListUsersActivity.class);
                startActivity(set_int);
                break;
            case R.id.aboutus:
                Intent st_int = new Intent(MainActivity.this,AboutUs.class);
                startActivity(st_int);

        }
        return super.onOptionsItemSelected(item);
    }
}
