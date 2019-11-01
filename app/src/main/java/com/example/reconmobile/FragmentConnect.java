package com.example.reconmobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentConnect extends Fragment {
    public FragmentConnect() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        Button btnConnect = view.findViewById(R.id.buttonConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FragmentConnect","Connect button pressed!");
                Toast txtOnClick_Connect = Toast.makeText(getContext(),"Searching for Recon...",Toast.LENGTH_SHORT);
                txtOnClick_Connect.show();
                showSearchList();
            }
        });

        return view;
    }

    //Show Device Search popup
    protected void showSearchList() {
        FragmentSearch dialogSearch = FragmentSearch.newInstance("arg1","arg2");
        dialogSearch.setRetainInstance(true);
        dialogSearch.show(getFragmentManager(),"");
    }

}
