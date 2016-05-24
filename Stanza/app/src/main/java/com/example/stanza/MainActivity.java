package com.example.stanza;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;

import java.util.Vector;

public class MainActivity extends AppCompatActivity
{
    /**
     * The layout manager that allows the user to flip left and right between the fragments
     * for each tab in the dashboard page.
     */
    ViewPager viewPager;
    /**
     * A toolbar to appear at the top of the activity UI
     */
    Toolbar toolbar;
    FloatingActionButton fab;


    /**
     * Called when the activiy is starting. Here is wherewe inflate the activity's UI using <code>setContentView(int)</code>
     * and programmatically set up UI elements, such as the floating action button and the tabs.
     * @param savedInstanceState  If the activity is being re-initialized after previously being shut down then
     *                            this Bundle contains the data it most recently supplied in <code>onSaveInstanceState(Bundle)</code>.
     *                            Otherwise, savedInstanceState is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);


        toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.mainViewPager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        tabLayout.setupWithViewPager(viewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        fab.show();
                        break;
                    case 1:
                        fab.hide();
                        break;
                }

            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * @param menu The options menu in which items are placed.
     * @return True for the menu to be displayed, false for the menu to not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *  Sets up the dashboard <code>ViewPager</code>, which manages the fragments
     *  that correspond to each tab.
     * @param viewPager The layout manager that allows the user to flip left and right between the fragments
     * for each tab in the dashboard page.
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new UserPoemFragment(), "MY POEMS");
        adapter.addFrag(new FriendBoardFragment(),"FRIEND BOARD");
        viewPager.setAdapter(adapter);

    }



}
