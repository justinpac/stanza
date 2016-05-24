package com.example.stanza;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of <code>PagerAdapter</code> that represents each page as a
 * <code>Fragment</code> that is persistently kept in the fragment manager as long as the user
 * can return to the page.
 */
class ViewPagerAdapter extends FragmentPagerAdapter {
    /**
     * The list of fragments that the user can scroll through.
     */
    private final List<Fragment> mFragmentList = new ArrayList<>();

    /**
     * The list of titles for each fragment to appear as tabs on the dashboard page.
     */
    private final List<String> mFragmentTitleList = new ArrayList<>();

    /**
     * The default constructor of a FragmentPagerAdapter using a fragment manager.
     * @param manager the interface for interacting with <code>Fragment</code> objects inside of an <code>Activity</code>.
     */
    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    /**
     * Get a fragment based on its position within the ViewPager.
     * @param position The position of the desired <code>Fragment</code> within the ViewPager.
     * @return The desired fragment.
     */
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    /**
     * Get the number of fragments in the ViewPager.
     * @return The number of fragments within the ViewPager.
     */
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * Add a fragment to the ViewPager.
     * @param fragment The fragment to be added.
     * @param title The title of the Fragment to be displayed as a tab.
     */
    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    /**
     * Get the title of a fragment based on its position.
     * @param position The position of the fragment.
     * @return The title of the fragment.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}