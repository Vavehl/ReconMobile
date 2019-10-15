package com.example.reconmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentConnect extends Fragment {
    View view;
    public FragmentConnect() {
    }

    private Button btnConnect;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connect,container,false);

        btnConnect = view.findViewById(R.id.buttonConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast txtOnClick_Connect = Toast.makeText(getContext(),"Searching for Recon...",Toast.LENGTH_SHORT);
                txtOnClick_Connect.show();
            }
        });

        return view;
    }
}
