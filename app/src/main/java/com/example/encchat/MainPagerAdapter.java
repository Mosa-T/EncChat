package com.example.encchat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class MainPagerAdapter extends FragmentPagerAdapter {


    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {


        switch (position){
            case 0: ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1: FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default: return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position){

            switch (position){
                case 0: return "Chats";
                case 1: return "Friends";
                default: return null;
            }

    }
}
