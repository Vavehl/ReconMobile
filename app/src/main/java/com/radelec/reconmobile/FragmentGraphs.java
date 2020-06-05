package com.radelec.reconmobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

public class FragmentGraphs extends Fragment {

    public FragmentGraphs() {
        Log.d("FragmentGraphs","FragmentGraphs() called!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("FragmentGraphs","onCreate() called!");
        super.onCreate(savedInstanceState);

        //Programmatically initialize ChartRadon tab -- there has to be a better way to implement this??
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.chartConstraintContainer, new ChartRadon(),"chartRadon");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        //End initial implementation of ChartRadon
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("FragmentGraphs","onCreateView() called!");
        View view = inflater.inflate(R.layout.fragment_graphs, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout_Graphs);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("FragmentGraphs","onTabSelected(TabLayout.Tab " + tab.toString() + ") called!");
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new ChartRadon();
                        break;
                    case 1:
                        fragment = new ChartPressure();
                        break;
                    case 2:
                        fragment = new ChartTemp();
                        break;
                    case 3:
                        fragment = new ChartHumidity();
                        break;
                    case 4:
                        fragment = new ChartTilts();
                        break;
                }
                if(fragment != null){
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.chartConstraintContainer, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d("FragmentGraphs","onTabUnselected(TabLayout.Tab " + tab.toString() + ") called!");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d("FragmentGraphs","onTabReselected(TabLayout.Tab " + tab.toString() + ") called!");
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new ChartRadon();
                        break;
                    case 1:
                        fragment = new ChartPressure();
                        break;
                    case 2:
                        fragment = new ChartTemp();
                        break;
                    case 3:
                        fragment = new ChartHumidity();
                        break;
                    case 4:
                        fragment = new ChartTilts();
                        break;
                }
                if(fragment != null) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.chartConstraintContainer, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }
            }

        });

        //return inflater.inflate(R.layout.fragment_graphs, container, false);
        return view;
    }
}
