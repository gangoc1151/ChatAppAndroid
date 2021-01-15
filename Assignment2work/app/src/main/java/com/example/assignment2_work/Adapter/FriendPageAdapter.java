package com.example.assignment2_work.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.assignment2_work.Fragment.FriendGalleryFragment;
import com.example.assignment2_work.Fragment.FriendSettingFragment;

public class FriendPageAdapter extends FragmentPagerAdapter {

    private int numOfTab;

    public FriendPageAdapter(@NonNull FragmentManager fm, int numOfTab) {
        super(fm, numOfTab);
        this.numOfTab = numOfTab;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new FriendSettingFragment();

            case 1:
                return new FriendGalleryFragment();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return numOfTab;
    }
}
