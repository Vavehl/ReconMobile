package com.example.reconmobile;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    public static String version_build = "v0.2.1";
    public static String version_date = "18 Oct 2019";

    DatabaseOperations db;

    //Create various dialog windows. Hopefully there won't be too many of them, or this is gonna become a mess...
    public Dialog dialogAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //At the very least, let's broadcast a few info log messages detailing the version info.
        Log.i("VersionInfo","Version Build = " + version_build);
        Log.i("VersionInfo", "Version Date = " + version_date);

        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tabLayout_Main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize database (in DatabaseOperations)
        db = new DatabaseOperations(this);

        //Programmatically initialize fragmentConnect tab -- there has to be a better way to implement this??
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleConstraintContainer, new FragmentConnect());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        //End initial implementation of fragmentConnect

        FloatingActionButton fab = findViewById(R.id.fabEmail);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "TODO: Email PDF Report", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override

            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new FragmentConnect();
                        break;
                    case 1:
                        fragment = new FragmentGraphs();
                        break;
                    case 2:
                        fragment = new FragmentReport();
                        break;
                }
                if(fragment != null){
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.simpleConstraintContainer, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new FragmentConnect();
                        break;
                    case 1:
                        fragment = new FragmentGraphs();
                        break;
                    case 2:
                        fragment = new FragmentReport();
                        break;
                }
                if(fragment != null) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.simpleConstraintContainer, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }
            }

        });
    }

    //Creates Recon Main Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.menu_about:
                showAboutPopUp();
                return true;
            case R.id.open_file:
                openFile();
                return true;
            case R.id.save_file:
                saveFile();
                return true;
            case R.id.settings:
                showSettings();
                return true;
            case R.id.menu_company:
                showMyCompany();
                return true;
            case R.id.menu_exit:
                System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    Assign version and build dates to the About dialog / pop-up window
    ...and then display it!
    */
    protected void showAboutPopUp() {

        //Initialize the "About" dialog window in the menu...
        dialogAbout = new Dialog(this);

        //Need to setContentView *before* setting the version and date textviews!
        dialogAbout.setContentView(R.layout.menu_about);

        //Initialize TextViews...
        final TextView tvAboutVersionBuild = dialogAbout.findViewById(R.id.aboutVersionBuild);
        final TextView tvAboutVersionDate = dialogAbout.findViewById(R.id.aboutVersionDate);

        //Initialize imgCloseAbout "button"
        final ImageView btnCloseAbout;
        btnCloseAbout = dialogAbout.findViewById(R.id.imgCloseAbout);

        //Initialize website link
        final TextView btnWebsite;
        btnWebsite = dialogAbout.findViewById(R.id.website_radelec);

        //Set the version and date textviews to their proper values.
        try {
            tvAboutVersionBuild.setText(version_build);
            tvAboutVersionDate.setText(version_date);
        } catch (NullPointerException ex) {
            Toast error = Toast.makeText(getApplicationContext(),"Unable to initialize version build and date!",Toast.LENGTH_SHORT);
            error.show();
        }

        //Allow user to close About window by clicking on the X in the upper right
        btnCloseAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAbout.dismiss();
            }
        });

        //Allow user to open Rad Elec website by clicking on the link
        btnWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intentRadElecWebsite = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.radelec.com"));
                    startActivity(intentRadElecWebsite);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "No web browser found.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Show what we have!
        dialogAbout.show();
    }

    //This will load/show the Settings fragment
    protected void showSettings() {
        Fragment fragment;
        fragment = new FragmentSettings();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleConstraintContainer, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    //This will load/show the Settings fragment
    protected void showMyCompany() {
        Fragment fragment;
        fragment = new FragmentCompany();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleConstraintContainer, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    //We should offload this to its own class...
    protected void saveFile() {
        Toast msgSave = Toast.makeText(getApplicationContext(),"Saving file...",Toast.LENGTH_SHORT);
        msgSave.show();
    }

    //We should offload this to its own class, too...
    protected void openFile() {
        Toast msgSave = Toast.makeText(getApplicationContext(),"TODO: Open File",Toast.LENGTH_SHORT);
        msgSave.show();
    }

}
