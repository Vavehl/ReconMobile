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

import static com.example.reconmobile.Globals.*;

public class FragmentConnect extends Fragment {

    Button btnConnect;

    public FragmentConnect() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        btnConnect = view.findViewById(R.id.buttonConnect);

        switch(connected) {
            case True:
                Log.d("FragmentConnect","Setting button to Disconnect [connected = " + connected + "]");
                btnConnect.setText("Disconnect");
                break;
            case False:
                Log.d("FragmentConnect","Setting button to Connect [connected = " + connected + "]");
                btnConnect.setText("Connect");
                break;
            case Pending:
                Log.d("FragmentConnect","Setting button to Wait [connected = " + connected + "]");
                btnConnect.setText("Wait");
                break;
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(connected) {
                    case True:
                        Log.d("FragmentConnect","Disconnect button pressed!");
                        Toast txtOnClick_Disconnect = Toast.makeText(getContext(),"Disconnecting...",Toast.LENGTH_SHORT);
                        txtOnClick_Disconnect.show();
                        disconnect();
                        checkConnectionStatus();
                        break;
                    case False:
                        Log.d("FragmentConnect","Connect button pressed!");
                        Toast txtOnClick_Connect = Toast.makeText(getContext(),"Searching for Recon...",Toast.LENGTH_SHORT);
                        txtOnClick_Connect.show();
                        showSearchList();
                        break;
                    case Pending:
                        Toast txtOnClick_Pending = Toast.makeText(getContext(),"Please wait...",Toast.LENGTH_SHORT);
                        txtOnClick_Pending.show();
                        break;
                }
            }
        });

        return view;
    }

    //Show Device Search popup
    protected void showSearchList() {
        Log.d("FragmentConnect", "showSearchList() called!");
        FragmentSearch dialogSearch = new FragmentSearch();
        dialogSearch.setRetainInstance(true);
        dialogSearch.show(getFragmentManager(),"");

        if(dialogSearch.isDetached()) {
            Log.d("FragmentConnect","SEARCH FRAGMENT DETACHED!");
            checkConnectionStatus();
        }
    }

    public void onStart() {
        Log.d("FragmentConnect", "onStart() called!");
        super.onStart();
    }

    public void onPause() {
        Log.d("FragmentConnect", "onPause() called!");
        super.onPause();
    }

    public void onResume() {
        Log.d("FragmentConnect", "onResume() called!");
        super.onResume();
    }

    public void checkConnectionStatus() {
        Log.d("FragmentConnect", "checkConnectionStatus() called!");
        switch(connected) {
            case True:
                Log.d("FragmentConnect","Setting button to Disconnect [connected = " + connected + "]");
                btnConnect.setText("Disconnect");
                break;
            case False:
                Log.d("FragmentConnect","Setting button to Connect [connected = " + connected + "]");
                btnConnect.setText("Connect");
                break;
            case Pending:
                Log.d("FragmentConnect","Setting button to Wait [connected = " + connected + "]");
                btnConnect.setText("Wait");
                break;
        }
    }

    private void disconnect() {
        Log.d("FragmentConnect","disconnect() called!");
        connected = ReconConnected.False;
        service.disconnect();
        socket.disconnect();
        socket = null;
    }

}
