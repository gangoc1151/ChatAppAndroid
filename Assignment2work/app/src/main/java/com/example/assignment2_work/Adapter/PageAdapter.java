package com.example.assignment2_work.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.assignment2_work.Fragment.GalleryFragment;
import com.example.assignment2_work.Fragment.SettingFragment;

public class PageAdapter extends FragmentPagerAdapter {

   private int numOfTab;
    /**Constructor of java class  */
    public PageAdapter(@NonNull FragmentManager fm, int numOfTab) {
        super(fm, numOfTab);
        this.numOfTab = numOfTab;
    }

    /**switch between two fragment by using tab layout */
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new SettingFragment();

            case 1:
                return new GalleryFragment();
            default:
                return null;
        }

    }
    /** return the size of  data set */
    @Override
    public int getCount() {
        return numOfTab;
    }
}
